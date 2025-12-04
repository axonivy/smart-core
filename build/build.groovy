def mavenIssues() {
  recordIssues tools: [mavenConsole()], qualityGates: [[threshold: 1, type: 'TOTAL']], filters: [
    // because every core bundle has a invalid patch.jar entry
    excludeMessage('.*Bundle-ClassPath entry .* does not exist in*'),
    excludeMessage('.*No digest algorithm is available to verify download*'),
    excludeMessage('.*Empty classpath of required bundle*'),
    excludeMessage('.*No explicit target runtime environment configuration*'),
    excludeMessage('.*Problems resolving provisioning plan*')
  ]
}

def collectJunit() {
  junit testDataPublishers: [[$class: 'StabilityTestDataPublisher']], testResults: '**/target/surefire-reports/**/*.xml'
}

return this
