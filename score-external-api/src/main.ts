import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ConfigService } from '@nestjs/config';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger'


async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  const config = new DocumentBuilder()
    .setTitle('connectCenter (Score) API')
    .setDescription('RESTful API for accessing standards and profiled BIE information and schemas from connectCenter also known as Score')
    .setVersion(process.env.npm_package_version)
    .addBearerAuth()
    .build()
  const document = SwaggerModule.createDocument(app, config)
  SwaggerModule.setup('api', app, document)

  const configService: ConfigService = app.get(ConfigService);
  await app.listen(configService.get('api_port'));
}
bootstrap();
