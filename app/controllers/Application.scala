package controllers

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.github.nscala_time.time.Imports._
import com.typesafe.config.ConfigFactory
import play.api.Play
import play.api.Play.current
import play.api.mvc._
import utils.TypeSafeConfigAWSCredentialsProvider

object Application extends Controller {

  val config = Play.configuration
  val credentialsProvider = config.getString("aws.credentialsProvider").getOrElse("DefaultAWSCredentialsProviderChain")
  val profileConfigName = config.getString("aws.profileCredentialName").getOrElse("default")
  val bucketName = config.getString("bucketName")

  def help() = Action {
    Ok("This application will sign an S3 url for download and redirect you to that url for the configured S3 bucket." +
      " Just base your file off the root of the bucket and put that in your URL. i.e. /somefolder/somefile.txt")
  }

  def download(file: String) = Action {
    bucketName match {
      case Some(x) => {
        val provider: AWSCredentialsProvider = credentialsProvider match {
          case "DefaultAWSCredentialsProviderChain" => new DefaultAWSCredentialsProviderChain()
          case "ProfileCredentialsProvider" => new ProfileCredentialsProvider(profileConfigName)
          case "TypeSafeConfigAWSCredentialsProvider" => new TypeSafeConfigAWSCredentialsProvider(ConfigFactory.load())
          case "InstanceProfileCredentialsProvider" => new InstanceProfileCredentialsProvider()
          case "ClasspathPropertiesFileCredentialsProvider" => new ClasspathPropertiesFileCredentialsProvider()
          case "EnvironmentVariableCredentialsProvider" => new EnvironmentVariableCredentialsProvider()
          case "SystemPropertiesCredentialsProvider" => new SystemPropertiesCredentialsProvider()
        }

        val s3Client = new AmazonS3Client(provider)
        val urlRequest = new GeneratePresignedUrlRequest(x.toString, file)
        urlRequest.setExpiration((DateTime.now + 30.seconds).date)

        val url = s3Client.generatePresignedUrl(urlRequest)
        TemporaryRedirect(url.toString)
      }

      case None => {
        InternalServerError("Missing configuration for bucketName")
      }
    }
  }
}