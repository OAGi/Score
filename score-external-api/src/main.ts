import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ConfigService } from '@nestjs/config';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger'


async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  const config = new DocumentBuilder()
    .setTitle('SCORE')
    .setDescription('Score External Facing API')
    .setVersion(process.env.npm_package_version)
    .build()
  const document = SwaggerModule.createDocument(app, config)
  SwaggerModule.setup('api', app, document)

  const configService: ConfigService = app.get(ConfigService);
  await app.listen(configService.get('api_port'));
}
bootstrap();
