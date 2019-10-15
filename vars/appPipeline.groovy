def call(Map pipelineParams) {
    pipeline {
        agent any
        parameters {
            choice choices: ['int', 'qa', 'dev'], description: "Choose which environment to push changes to.", name: "DEPLOY_TO"
        }

        environment {
            DEPLOY_TO = "${params.DEPLOY_TO}"
            INT_PH_IP = '172.28.27.150'
            INT_US_IP = '172.31.0.240'
        }
        stages {
            stage('archive') {
                steps {
                    archiveArtifacts pipelineParams.archive
                }
            }
            stage('Deploy to INT-PH') {
                when {
                    anyOf { branch 'feature-*'; branch 'develop'; branch 'master' } 
                    environment ignoreCase: true, name: "DEPLOY_TO", value: "int"
                }
                stages {
                    stage('init') {
                        steps {
                            echo 'WORKSPACE '+env.WORKSPACE
                            echo 'JENKINS_HOME '+env.JENKINS_HOME
                            echo 'Deploying to ' + env.DEPLOY_TO
                            echo 'Branch name ' + env.BRANCH_NAME
                            echo 'Build number ' + env.BUILD_NUMBER
                            echo 'Git URL ' + env.GIT_URL
                            echo 'JENKINS_URL '+JENKINS_URL
                            echo 'BUILD_URL '+BUILD_URL
                            echo 'JOB_URL '+JOB_URL
                        }
                    }
                    stage('Build core') {
                        when {
                            // expression { env.GIT_URL.contains('kjt-pos-callctr-client') }
                            equals expected: 'kjt-pos-core', actual: pipelineParams.project
                        }
                        steps {
                            echo "Building ${pipelineParams.project}"
                            maven clean compile war:war     
                        }
                    } 
                }
                post {
                    always {
                        echo "Deploying ${pipelineParams.project}"
                        deploy(pipelineParams.type, pipelineParams.project, env.INT_PH_IP, env.BUILD_NUMBER, 'int', env.BRANCH_NAME)
                    }
                }
            }

            stage('Deploy to QA') {
                when {
                    environment ignoreCase: true, name: "DEPLOY_TO", value: "qa"
                }
                steps {
                    echo 'Deploying to qa'
                    echo 'Branch name ' + env.BRANCH_NAME
                }
            }
        }
    }
}