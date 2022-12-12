pipeline {
    agent any 	// 사용 가능한 에이전트에서 이 파이프라인 또는 해당 단계를 실행
    stages {
        stage('Prepare') {
            steps {
                git url: 'https://github.com/Laboratory-Information-System-Project/gatewayService.git',
                    branch: 'develop',
                    credentialsId: 'data-service'
            }

            post {
                success {
                    sh 'echo "Successfully Cloned Repository"'
                }
                failure {
                    sh 'echo "Fail Cloned Repository"'
                }
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh  './gradlew clean build'
                sh 'ls -al ./build'
            }
            post {
                success {
                    echo 'gradle build success'
                }

                failure {
                    echo 'gradle build failed'
                }
            }

        }
//         stage('Test') {
//             steps {
//                 echo  '테스트 단계와 관련된 몇 가지 단계를 수행합니다.'
//             }
//         }
//         stage('Docker Rm') {
//             steps {
//                 sh 'echo "Docker Rm Start"'
//                 sh """
//                 docker stop deploy-test:0.0.1
//                 docker rm -f deploy-test:0.0.1
//                 docker rmi -f suk97/deploy-test:0.0.1
//                 """
//             }
//
//             post {
//                 success {
//                     sh 'echo "Docker Rm Success"'
//                 }
//                 failure {
//                     sh 'echo "Docker Rm Fail"'
//                 }
//             }
//         }

        stage('Dockerizing'){
            steps{
                sh 'echo " Image Bulid Start"'
                sh 'docker build . -t suk97/lis-gateway'
            }
            post {
                success {
                    sh 'echo "Bulid Docker Image Success"'
                }

                failure {
                    sh 'echo "Bulid Docker Image Fail"'
                }
            }
        }

        stage('push'){
            steps {
                echo 'Push Docker'
                    script {
                        docker.withRegistry('https://registry.hub.docker.com', 'docker'){
                            sh 'docker login -u "suk97" -p "ehddnr0511@" docker.io'
                            sh 'docker push suk97/lis-gateway'
                        }
                    }
            }
        }

         stage('Run Container on SSH Dev Server'){
                    steps{
                        echo 'SSH'

                        sshagent (credentials: ['jenkins-server-privatekey']) {
//                              sh "eval ${ssh-agent -s}"
                             sh "ssh -o StrictHostKeyChecking=no ubuntu@175.41.222.118 'sudo docker pull suk97/lis-dataservice'"
                             sh "ssh -o StrictHostKeyChecking=no ubuntu@175.41.222.118 'sudo docker rm -f lis-dataservice'"
                             sh "ssh -o StrictHostKeyChecking=no ubuntu@175.41.222.118 'sudo docker run -d --name dataservice -p 8080:8080 suk97/lis-dataservice'"
                        }

                    }

                }

    }
}