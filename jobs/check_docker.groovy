pipelineJob('check-docker-pipeline-job') {
    description('A pipeline job to check Docker CLI.')
    
    definition {
        cps {
            script('''
                pipeline {
                    agent any
                    stages {
                        stage('Verify Docker CLI') {
                            steps {
                                // Check if the CLI is available
                                sh 'docker --version'
                                // Check if it can talk to the host's daemon successfully
                                sh 'docker ps'
                            }
                        }
                    }
                }
            '''.stripIndent())
        }
    }
}