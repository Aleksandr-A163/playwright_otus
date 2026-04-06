pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        choice(
            name: 'BROWSER',
            choices: ['chromium', 'firefox', 'webkit'],
            description: 'Browser for UI tests'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run tests in headless mode'
        )
        string(
            name: 'BASE_URL',
            defaultValue: 'https://otus.ru/lessons/clickhouse/',
            description: 'Base URL for TeachersUiTest'
        )
        string(
            name: 'SITE_URL',
            defaultValue: 'https://otus.ru',
            description: 'Site URL for CatalogCoursesFilterTest and SubscriptionTest'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run UI tests') {
            steps {
                sh '''
                  set -eux
                  chmod +x gradlew
                  ./gradlew --no-daemon clean test \
                    -Dbrowser="${BROWSER}" \
                    -Dheadless="${HEADLESS}" \
                    -DbaseUrl="${BASE_URL}" \
                    -DsiteUrl="${SITE_URL}"
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/reports/**, build/test-results/**, build/allure-results/**, traces/**', allowEmptyArchive: true

            allure([
                includeProperties: false,
                results: [[path: 'build/allure-results']]
            ])
        }
    }
}
