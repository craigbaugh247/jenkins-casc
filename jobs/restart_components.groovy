pipelineJob('restart-components-job') {
    description('A pipeline job to restart components.')
    
    definition {
        cps {
            script('''
                @Library('shared-lib') _
                pipeline {
                    agent any
                    parameters {
                        choice(name: 'COMPONENT', choices: getComponentsDropDown(), description: 'Select the component')
                    }
                    stages {
                      stage('Example') {
                        steps {
                          echo "Selected component: ${params.COMPONENT}"
                        }
                      }
                    }
                }
            '''.stripIndent())
        }
    }
}