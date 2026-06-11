pipelineJob('manage-containers-job') {
    description('A pipeline job to manage containers.')
    
    definition {
        cps {
            script('''
                @Library('shared-lib') _
                pipeline {
                    agent any
                    parameters {
                        choice(name: 'CONTAINER', choices: listContainers(), description: 'Select the container')
                        choice(name: 'ACTION', choices: manageContainerActions(), description: 'Select the action')
                    }
                    stages {
                      stage('Performing Action') {
                        steps {
                          sh 'echo "${params.ACTION}ing container ${params.CONTAINER}"'
                          sh 'docker ${params.ACTION} ${params.CONTAINER}'
                        }
                      }
                    }
                    post {
                        success {
                            sendSlackMessage(status: 'SUCCESS', channel: '#Jenkins')
                        }
                        failure {
                             sendSlackMessage(status: 'FAILURE', channel: '#Jenkins')
                        }
                    }
                }
            '''.stripIndent())
        }
    }
}