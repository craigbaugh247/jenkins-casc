pipelineJob('prometheus-rules-pipeline-job') {
    description('A pipeline job to test and deploy Prometheus rules.')
    
    definition {
        cps {
            script('''
                pipeline {
                    agent any

                    environment {
                        GIT_SSH_COMMAND = 'ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null'
                        PROM_CONTAINER_NAME = 'prometheus'
                        PROM_RULES_DIR     = '/etc/prometheus/rules/'
                        PROM_RELOAD_URL    = 'http://prometheus:9090/-/reload'
                    }

                    stages {
                        stage('Checkout Code') {
                          steps {
                            checkout scmGit(
                              branches: [[name: 'main']],
                              userRemoteConfigs: [[
                                url: 'git@github.com:craigbaugh247/prometheus_rules.git',
                                credentialsId: 'github-ssh-key'
                              ]]
                            )
                          }
                        }

                        stage('Validate Rules') {
                            steps {
                                echo 'Validating Prometheus rule files using promtool...'
                                
                                // Runs a temporary promtool container to check all yaml files in the rules directory
                                sh 'docker run --rm --entrypoint /bin/sh -v $(pwd)/rules:/tmp/rules prom/prometheus:latest -c "promtool check rules /tmp/rules/*_rules.yml"'
                            }
                        }

                        stage('Deploy Rules') {
                            steps {
                                echo "Copying rule files to container: \${PROM_CONTAINER_NAME}..."
                                
                                // Clear out old rules inside the container and copy the new ones over
                                sh 'docker exec ${PROM_CONTAINER_NAME} rm -rf ${PROM_RULES_DIR}* && docker cp rules/. ${PROM_CONTAINER_NAME}:${PROM_RULES_DIR}'
                            }
                        }

                        stage('Reload Prometheus') {
                            steps {
                                echo 'Triggering Prometheus configuration dynamic reload...'
                                
                                // Send a POST request to the API reload endpoint to apply changes instantly
                                sh 'curl -X POST -s -o /dev/null -w "%{http_code}" ${PROM_RELOAD_URL} | grep 200'
                            }
                        }
                    }

                    post {
                        success {
                            echo 'Prometheus rules deployed and reloaded successfully!'
                        }
                        failure {
                            echo 'Deployment failed! Check the step output for errors.'
                        }
                    }
                }
            '''.stripIndent())
        }
    }
}