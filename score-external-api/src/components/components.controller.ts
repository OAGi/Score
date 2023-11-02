import { Controller, UseGuards, UseInterceptors } from '@nestjs/common';
import { Get, Req } from '@nestjs/common';
import { ComponentsService } from './components.service';
import { Param, Query } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AuthGuard } from 'src/auth/auth.guard';

@Controller('api/components')
@UseGuards(AuthGuard)
export class ComponentsController {

  constructor(private readonly componentsService: ComponentsService, private configService: ConfigService) { }

  @Get()
  findAll(
    @Query('tags') tags,
    @Query('release') release,
    @Query('types') componentTypes,
  ) {
    return this.componentsService.getAllComponentsMetadata(tags, release, componentTypes);
  }


  @Get('uuid/:uuid')
  findComponent(
    @Param('uuid') uuid: string,
    @Query('release') release,
    @Query('schemaType') schemaType
  ) {
      return this.componentsService.getStandaloneComponent(uuid, schemaType, release);
  }

  @Get('releases')
  getReleases() {
    return this.componentsService.getReleases();
  }


  @Get('latest_release')
  getLocal() {
    return this.componentsService.getLatestRelease();
  }

}

