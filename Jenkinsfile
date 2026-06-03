pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID     = credentials('aws-account-id')
        AWS_REGION         = 'us-east-1'
        ECR_REPO           = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/lifeguard-insurance"
        IMAGE_TAG          = "${env.BUILD_NUMBER}"
        KUBECONFIG_CRED    = 'eks-kubeconfig'
        SONAR_TOKEN        = credentials('sonarqube-token')
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/your-org/lifeguard-insurance.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Quality - SonarQube') {
            steps {
                sh """
                  mvn sonar:sonar \
                    -Dsonar.host.url=http://sonarqube:9000 \
                    -Dsonar.login=${SONAR_TOKEN}
                """
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${ECR_REPO}:${IMAGE_TAG} ."
                sh "docker tag ${ECR_REPO}:${IMAGE_TAG} ${ECR_REPO}:latest"
            }
        }

        stage('Push to AWS ECR') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                      aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REPO}
                      docker push ${ECR_REPO}:${IMAGE_TAG}
                      docker push ${ECR_REPO}:latest
                    """
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                withKubeConfig([credentialsId: "${KUBECONFIG_CRED}"]) {
                    sh """
                      sed -i 's|IMAGE_PLACEHOLDER|${ECR_REPO}:${IMAGE_TAG}|g' \
                        kubernetes/deployment.yaml
                      kubectl apply -f kubernetes/
                      kubectl rollout status deployment/lifeguard-app -n lifeguard
                    """
                }
            }
        }

        stage('Smoke Test') {
            steps {
                sh """
                  sleep 15
                  curl -f http://lifeguard-app/actuator/health || exit 1
                """
            }
        }
    }

    post {
        success {
            slackSend channel: '#deployments',
                      color: 'good',
                      message: "✅ LifeGuard build #${BUILD_NUMBER} deployed successfully!"
        }
        failure {
            slackSend channel: '#deployments',
                      color: 'danger',
                      message: "❌ LifeGuard build #${BUILD_NUMBER} FAILED. Check Jenkins."
        }
    }
}
