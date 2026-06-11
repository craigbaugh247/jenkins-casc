pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }
        stage('Process Job DSL') {
            steps {
                jobDsl(
                    targets: '''jobs/**/*.groovy''',
                    removedJobAction: 'DELETE',
                    removedViewAction: 'DELETE',
                    lookupStrategy: 'JENKINS_ROOT'
                )
            }
        }
    }
}