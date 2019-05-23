node('aso-dev-2') {
	def checkoutBranch = 'release2'
	def repositoryUrl = env.GIT_URL
	currentBuild.result = "SUCCESS"
	try {
	   def mavenName = '/var/lib/jenkins/tools/hudson.tasks.Maven_MavenInstallation/mvn-3.x.x'
	   stage('Checkout'){
			sh "env"
			checkout([$class: 'GitSCM', branches: [[name: "*/${checkoutBranch}"]], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'a64d3cf0-4b9c-4801-858c-2109152bcaf9', url: 'https://bitbucket.org/academysports/api-cart.git']]])
	   }
	   stage('Building Dependent Projects'){
	       sh '''
	          currentDir=$(pwd)
	          cd ../dev2_Aggregator_Framework/
	          if [ "$( git status | grep HEAD | awk '{print $NF}' )" != "$( git rev-parse refs/remotes/origin/master | cut -b 1-7 )" ]
	          then
	              echo -e "1" > ${currentDir}/variable.properties
	              cd ${currentDir}
	          else
			      echo -e "0" > ${currentDir}/variable.properties
				  cd ${currentDir}
			  fi
		   '''
		   env.WORKSPACE = pwd()
           def gitVerDiff = readFile "${env.WORKSPACE}/variable.properties"
		   if(gitVerDiff=='1')
		   { 
		        build 'dev2_Aggregator_Framework'
		   }
		   sh '''
		      rm -f variable.properties
		   '''
	   }
	   stage('Build'){
		    sh "${mavenName}/bin/mvn clean install -DskipTests checkstyle:checkstyle findbugs:findbugs pmd:pmd pmd:cpd cobertura:cobertura -Dcobertura.report.format=xml"
	   }
	   //stage('UnitTest'){
	     //  sh "${mavenName}/bin/mvn test -B"
	      // step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
	  // }
	   stage('Code Quality - Sonar'){
		   sh "${mavenName}/bin/mvn -e -B sonar:sonar -Dsonar.java.source=1.8 -Dsonar.surefire.reportsPath=target/surefire-reports -Dsonar.dynamicAnalysis=reuseReports -Dsonar.cobertura.reportPath=target/site/cobertura/coverage.xml -Dsonar.host.url=http://35.188.97.214 -Dsonar.login=754b09156c95e1d1b41a3d10830ea25b3f1a49b6 -Dsonar.sourceEncoding=UTF-8"
	   }
	   stage('Build Docker Image'){
		   sh '''
				cd ${WORKSPACE}
				imageName="us.gcr.io/just-slate-88918/api_cart_dev2:${BUILD_NUMBER}"
				imageNameLatest="us.gcr.io/just-slate-88918/api_cart_dev2:latest"
				if [ $( sudo docker image list | grep -c ${imageName} ) -eq 1 ]
				then 
					sudo docker rmi ${imageName}
				fi
				sudo docker build -t ${imageName} --pull=true --file=src/main/docker/Dockerfile ${WORKSPACE}
				sudo docker tag ${imageName} ${imageNameLatest}
			'''
	   }
	   parallel 'Publish Docker Image':{
		   sh "sudo gcloud docker -- push us.gcr.io/just-slate-88918/api_cart_dev2:${BUILD_NUMBER}"
		   sh "sudo gcloud docker -- push us.gcr.io/just-slate-88918/api_cart_dev2:latest"
	   }
	   if(currentBuild.result=='SUCCESS')
		{
			stage('Deploying to Kubernetes'){
				sh '''
					serviceName='apicart'
					serviceType='deployment'
					export PATH=$HOME/google-cloud-sdk/bin:$PATH
					if [ ! -z ${serviceName} ] && [ ! -z ${serviceType} ]
					then
						cd src/main/kube
						if [ $( echo -e ${serviceType} | grep -c deployment ) -eq 1 ] && [ $( kubectl get service | grep -c ${serviceName} ) -eq 0 ]
						then
						    kubectl create -f service.yml --save-config
							sleep 10s
							ip=`kubectl get svc | grep ${serviceName} | awk '{print $4}'` 
							#echo "$ip"
							sed -i "s/IP/${ip}/g" ${serviceType}".yml"
						else     
							if [ $( kubectl get ${serviceType} | grep -c ${serviceName} ) -ge 1 ]
							then
							    sleep 10s
							    ip=`kubectl get svc | grep ${serviceName} | awk '{print $4}'`
								#echo "$ip"
							    sed -i "s/IP/${ip}/g" ${serviceType}".yml"
								kubectl apply -f $( echo -e ${serviceType}".yml" )
								for podName in $( echo -e $( kubectl get pods | grep "${serviceName}-" | awk '{print $1}' ) )
								do
								    kubectl delete pods ${podName}
								done
							else
								kubectl create --save-config -f $( echo -e ${serviceType}".yml" )

							fi
						fi
					fi
				'''
			}
		}
	}
	catch (err) {
        currentBuild.result = "FAILURE"
            step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'hkhan2@sapient.com mramanathan@sapient.com', sendToIndividuals: false])
        throw err
    }
	finally{
		if(currentBuild.result!='FAILURE')
		{
			sleep 120
			parallel 'Performance-tests':{
			    build job: 'Gatling', wait: false
			}, 'Functional-Test':{
			    build job: 'Automation', wait: false
			}
		}
	}
}
