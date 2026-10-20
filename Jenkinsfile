import org.jenkinsci.plugins.credentialsbinding.impl.CredentialNotFoundException

def gitCommit = ''
def compileOutcome = ''
def testsOutcome = ''

pipeline {
    agent { label 'java-jdk21' }

    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactDaysToKeepStr: '14'))
    }

    parameters {
        choice(name: 'BROWSER_SIZE', choices: ['1366x768', '1920x1080'], description: 'Browser window size')
    }

    stages {
        stage('Validate parameters') {
            steps {
                sh '''
                    case "$BROWSER_SIZE" in
                      1366x768|1920x1080) ;;
                      *)
                        echo "Unsupported BROWSER_SIZE parameter"
                        exit 1
                        ;;
                    esac
                '''
            }
        }

        stage('Check out repository') {
            steps {
                deleteDir()
                script {
                    gitCommit = checkout(scm).GIT_COMMIT
                }
            }
        }

        stage('Install Allure Report') {
            steps {
                sh 'npm ci'
            }
        }

        stage('Print tool versions') {
            steps {
                sh '''
                    java -version
                    node --version
                    npx allure --version
                    ./gradlew --version
                '''
            }
        }

        stage('Compile tests') {
            steps {
                script {
                    compileOutcome = 'failure'
                    sh './gradlew testClasses --console=plain'
                    compileOutcome = 'success'
                }
            }
        }

        stage('Run tests') {
            steps {
                script {
                    testsOutcome = 'failure'
                    withCredentials([string(credentialsId: 'selenoid-sirdumowka-remote-url', variable: 'SELENOID_REMOTE_URL')]) {
                        sh './gradlew test -Dselenide.headless=true "-Dselenide.browserSize=$BROWSER_SIZE" "-Dselenide.remote=$SELENOID_REMOTE_URL" --console=plain'
                    }
                    testsOutcome = 'success'
                }
            }
        }
    }

    post {
        always {
            stash name: 'notify', includes: 'scripts/telegram_notify.py, build/test-results/test/TEST-*.xml', allowEmpty: true
            script {
                if (testsOutcome) {
                    archiveArtifacts artifacts: 'build/allure-results/**', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'build/test-results/test/**, build/reports/tests/test/**'
                    junit testResults: 'build/test-results/test/TEST-*.xml'
                    def hasAllureResults = sh(
                        script: 'set -- build/allure-results/*-result.json; [ -e "$1" ]',
                        returnStatus: true
                    ) == 0
                    if (!hasAllureResults) {
                        error 'No Allure results found in build/allure-results'
                    }
                    sh 'npx allure generate build/allure-results --output build/allure-report --clean'
                    archiveArtifacts artifacts: 'build/allure-report/**'
                }
            }
        }

        cleanup {
            script {
                if (currentBuild.currentResult == 'ABORTED') {
                    echo 'Build aborted: Telegram notification skipped'
                    return
                }
                def notifyEnv = [
                    'NOTIFY_PROJECT=Dumowka/getting-started-java',
                    'NOTIFY_CI=Jenkins',
                    "NOTIFY_BRANCH=${env.BRANCH_NAME ?: ''}",
                    "NOTIFY_COMMIT=${gitCommit}",
                    "NOTIFY_RUN_URL=${env.BUILD_URL ?: ''}",
                    "NOTIFY_TEST_JOB_RESULT=${currentBuild.currentResult == 'SUCCESS' ? 'success' : 'failure'}",
                    "NOTIFY_COMPILE_OUTCOME=${compileOutcome}",
                    "NOTIFY_TESTS_OUTCOME=${testsOutcome}",
                ]
                node('python-python314') {
                    deleteDir()
                    unstash 'notify'
                    withEnv(notifyEnv) {
                        try {
                            withCredentials([
                                string(credentialsId: 'sirdumowka-telegram-bot-token', variable: 'TELEGRAM_BOT_TOKEN'),
                                string(credentialsId: 'sirdumowka-telegram-chat-id', variable: 'TELEGRAM_CHAT_ID'),
                            ]) {
                                sh 'python3 scripts/telegram_notify.py build/test-results/test'
                            }
                        } catch (CredentialNotFoundException e) {
                            echo "Telegram credentials not found: ${e.message}"
                            sh 'python3 scripts/telegram_notify.py build/test-results/test'
                        }
                    }
                }
            }
        }
    }
}
