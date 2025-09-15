pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '60', artifactNumToKeepStr: '2'))
  }

  triggers {
    cron 'H 23 * * *'
  }

  stages {
    stage('build') {
      agent {
        dockerfile {
          dir '.'
          reuseNode true
        }
      }
      steps {
        script {
          maven cmd: "clean verify -DSKIP_UNIT_TESTS=false --settings maven/config/settings.xml"
          discoverGitReferenceBuild defaultBranch: 'master'

          recordIssues tools: [mavenConsole()], qualityGates: [[threshold: 1, type: 'TOTAL']], filters: [
            // because every core bundle has a invalid patch.jar entry
            excludeMessage('.*Bundle-ClassPath entry .* does not exist in*'),
            excludeMessage('.*No digest algorithm is available to verify download*'),
            excludeMessage('.*Empty classpath of required bundle*'),
            excludeMessage('.*No explicit target runtime environment configuration*'),
            excludeMessage('.*Problems resolving provisioning plan*')
          ]
          junit testDataPublishers: [[$class: 'StabilityTestDataPublisher']], testResults: '**/target/surefire-reports/**/*.xml'
          archiveArtifacts 'ch.ivyteam.smart.p2/target/*.zip'
        }
      }
    }

    stage('deploy') {
      when { expression { isReleasingBranch() } }
      steps {
        script {
          def p2Name = 'ch.ivyteam.smart.p2'
          def p2Project = p2Name + '/target/'
          def p2RepoZip = findFiles glob: p2Project + '/*.zip'
          def version = p2RepoZip[0].name.replace(p2Name, '').replace('SNAPSHOT.zip', '').replace('-', '')
          def repo = deployP2Repo(srcDir: p2Project + 'repository/', name: 'smart-core', version: version)
          echo "p2 repository available under ${repo.httpUrl}"
          currentBuild.description = "<a href='${repo.httpUrl}'>p2-repo</a>"
        }
      }
    }
  }
}
