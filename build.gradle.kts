plugins {
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.intelliJPlatform)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
  jvmToolchain(21)
}

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity(providers.gradleProperty("platformVersion"))

    bundledPlugins(
      providers.gradleProperty("platformBundledPlugins")
        .map { it.split(',').filter(String::isNotBlank) })
    plugins(
      providers.gradleProperty("platformPlugins").map { it.split(',').filter(String::isNotBlank) })
  }
}

intellijPlatform {
  pluginConfiguration {
    name = providers.gradleProperty("pluginName")
    version = providers.gradleProperty("pluginVersion")

    ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
      untilBuild = provider { null }
    }
  }

  signing {
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("PRIVATE_KEY")
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
  }

  publishing {
    token = providers.environmentVariable("PUBLISH_TOKEN")
  }

  pluginVerification {
    ides {
      recommended()
    }
  }
}
