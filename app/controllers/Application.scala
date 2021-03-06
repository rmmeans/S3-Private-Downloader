package controllers

import com.amazonaws.HttpMethod
import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.github.nscala_time.time.Imports._
import com.rmeans.s3signer.appversion.BuildInfo
import play.api.Play
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc._
import play.utils.UriEncoding
import utils.TypesafeConfigAWSCredentialsProvider

object Application extends Controller {

  val config = Play.configuration
  val credentialsProvider = config.getString("aws.credentialsProvider").getOrElse("DefaultAWSCredentialsProviderChain")
  val profileConfigName = config.getString("aws.profileCredentialName").getOrElse("default")
  val bucketName = config.getString("bucketName")

  def help() = Action {
    Ok("This application will sign an S3 url for download and redirect you to that url for the configured S3 bucket." +
      " Just base your file off the root of the bucket and put that in your URL. i.e. /somefolder/somefile.txt")
  }

  def download(file: String) = Action { implicit request =>
    bucketName match {
      case Some(bucket) => {
        val provider: AWSCredentialsProvider = credentialsProvider match {
          case "DefaultAWSCredentialsProviderChain" => new DefaultAWSCredentialsProviderChain()
          case "ProfileCredentialsProvider" => new ProfileCredentialsProvider(profileConfigName)
          case "TypesafeConfigAWSCredentialsProvider" => new TypesafeConfigAWSCredentialsProvider(config)
          case "InstanceProfileCredentialsProvider" => new InstanceProfileCredentialsProvider()
          case "ClasspathPropertiesFileCredentialsProvider" => new ClasspathPropertiesFileCredentialsProvider()
          case "EnvironmentVariableCredentialsProvider" => new EnvironmentVariableCredentialsProvider()
          case "SystemPropertiesCredentialsProvider" => new SystemPropertiesCredentialsProvider()
        }

        val urlToSign = UriEncoding.decodePath(file, "UTF-8")
        val s3Client = new AmazonS3Client(provider)
        val currentDate = DateTime.now.toDateTime(DateTimeZone.UTC)
        val urlRequest = new GeneratePresignedUrlRequest(bucket.toString, urlToSign, httpMethodConverter(request.method))
          .withExpiration((currentDate + 30.seconds).date)

        TemporaryRedirect(s3Client.generatePresignedUrl(urlRequest).toString)
      }

      case None => {
        InternalServerError("Missing configuration for bucketName")
      }
    }
  }

  def httpMethodConverter(method: String): HttpMethod = {
    method.toUpperCase match {
      case "GET" => HttpMethod.GET
      case "POST" => HttpMethod.POST
      case "PUT" => HttpMethod.PUT
      case "DELETE" => HttpMethod.DELETE
      case "HEAD" => HttpMethod.HEAD
      case "PATCH" => HttpMethod.PATCH
    }
  }

  def info() = Action {
    Ok(
      Json.obj(
        "buildInfo" -> Json.obj(
          "appName" -> BuildInfo.name,
          "version" -> BuildInfo.version,
          "gitCommit" -> BuildInfo.gitCommit,
          "buildTime" -> BuildInfo.buildTime
        )
      )
    )
  }
}
