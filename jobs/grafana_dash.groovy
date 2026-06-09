pipelineJob('grafana-dashboards-pipeline-job') {
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
                        stage('Build Go App') {
                            steps {
                                // 1. Invoke the Go toolset installed by the plugin
                                golang('go-1.26.0') { // Match your exact global tool configuration name
                                    
                                    // 2. Ensure Go modules are turned on explicitly
                                    withEnv(['GO111MODULE=on']) {
                                        
                                        // 3. MANDATORY: Point to the directory containing your go.mod
                                        // Change 'src/my-project' if your code lives in a subdirectory
                                        dir('.') { 
                                            
                                            // 4. Always tidy modules first to pull dependencies
                                            sh 'go mod tidy'
                                            
                                            // 5. Run your build or tests
                                            sh 'go build -o myapp main.go'
                                        }
                                    }
                                }
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