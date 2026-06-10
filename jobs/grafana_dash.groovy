pipelineJob('grafana/deploy-grafana-dashboards') {
    description('A pipeline job to check Docker CLI.')
    
    definition {
        cps {
            script('''
                pipeline {
                    agent any
                    tools { go 'go' }

                    environment {
                        // Define your Grafana server instance details globally
                        GRAFANA_SERVER   = 'http://grafana:3000'
                    }

                    stages {
                        stage('Check Go Version') {
                          steps {
                            sh 'go version'
                          }
                        }
                        stage('Checkout Code') {
                          steps {
                            checkout scmGit(
                              branches: [[name: 'main']],
                              userRemoteConfigs: [[
                                url: 'git@github.com:craigbaugh247/grafana_dash.git',
                                credentialsId: 'github-ssh-key'
                              ]]
                            )
                          }
                        }

                        stage('Generate Dashboard') {
                            steps {
                                // compile payload dynamically:
                                sh 'go mod download github.com/grafana/grafana-foundation-sdk/go && go mod tidy'
                                sh 'go run main.go' 
                                
                                // Confirm the target dashboard file exists
                                sh 'ls -la ./dashboard.json'
                            }
                        }

                        stage('Deploy to Grafana') {
                            steps {
                                // Bind the JCasC credential ID into environment variables
                                withCredentials([usernamePassword(
                                    credentialsId: 'external-service-credentials-id', 
                                    usernameVariable: 'GRAFANA_USER', 
                                    passwordVariable: 'GRAFANA_TOKEN'
                                )]) {
                                    script {
                                        // Deploy the dashboard using the gcx tool
                                        sh 'gcx config set contexts.default.grafana.user admin'
                                        sh 'gcx config set contexts.default.grafana.password admin'
                                        sh 'gcx resources push dashboards --path ./dashboard.json'
                                    }
                                }
                            }
                        }
                    }
                    
                    post {
                        always {
                            cleanWs()
                        }
                    }
                }
            '''.stripIndent())
        }
    }
}