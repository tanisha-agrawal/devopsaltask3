job("Job1_t6"){
  description("Pulling code")
	scm {
		github('tanisha-agrawal/devopsaltask3','master')
	}
     		
	triggers {
		scm("* * * * *")
	}
     steps {
        shell('sudo cp * /jenkins/')
    }
}
job("Job2_t6"){
  description("Creating and exposing deployment")
	triggers {
        upstream('Job1_t6', 'SUCCESS')
    }
     steps {
        shell('''
		if sudo docker ps | grep kubec
then
  sudo docker rm -f kubec
else
  echo "No kubec"
fi

sudo docker run -dit -v /jenkins:/task3 --name kubec tanisha30/kubec:v2
sudo docker exec kubec kubectl delete all --all --server https://192.168.99.102:8443 --client-key /home/jenkins/client.key --client-certificate /home/jenkins/client.crt --certificate-authority /home/jenkins/ca.crt
sudo docker exec kubec kubectl delete pvc --all --server https://192.168.99.102:8443 --client-key /home/jenkins/client.key --client-certificate /home/jenkins/client.crt --certificate-authority /home/jenkins/ca.crt
 
if ls /jenkins | grep php
then
  sudo docker exec kubec kubectl create -f /task3/web-php.yaml --server https://192.168.99.102:8443 --client-key /home/jenkins/client.key --client-certificate /home/jenkins/client.crt --certificate-authority /home/jenkins/ca.crt
elif ls /jenkins | grep html 
then
  sudo docker exec kubec kubectl create -f /task3/web-html.yaml --server https://192.168.99.102:8443 --client-key /home/jenkins/client.key --client-certificate /home/jenkins/client.crt --certificate-authority /home/jenkins/ca.crt
else
  exit 1
fi
		''')
    }
}
job("Job3_t6")
{
	description("Testing the app")
	triggers {
        upstream('Job2_t6', 'SUCCESS')
    }
	 steps {
        shell('''
export status=$(curl -s -i -w "%{http_code}" -o /dev/null/ http://192.168.99.102:32004)
if echo $status==200
then
  exit 1
else
  exit 0
fi
	''')
      }
}

job("Job4_t6")
{
	description("Sending email")
	triggers {
        upstream('Job3_t6', 'SUCCESS')
    }
	 publishers {
	        extendedEmail {
	            recipientList('tanishaagrawal016@gmail.com@gmail.com')
	            defaultSubject('Job status')
	          	attachBuildLog(attachBuildLog = true)
	            defaultContent('Status Report')
	            contentType('text/html')
	            triggers {
	                always {
	                    subject('build Status')
	                    content('Body')
	                    sendTo {
	                        developers()
	                        recipientList()
	                    }
			       }
		       }
		   }
	  }
}

buildPipelineView('Task-6 view') {
    filterBuildQueue()
    filterExecutors()
    title('Groovy Pipeline')
    displayedBuilds(1)
    selectedJob('Job1_t6')
    refreshFrequency(3)
}

