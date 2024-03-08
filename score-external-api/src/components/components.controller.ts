import { Controller, UseGuards, UseInterceptors } from '@nestjs/common';
import { Get, Req } from '@nestjs/common';
import { ComponentsService } from './components.service';
import { Param, Query } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AuthGuard } from 'src/auth/auth.guard';
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags, ApiQuery } from '@nestjs/swagger';


@ApiTags('connectSpec (OAGIS) Components')
@Controller('api/components')
@UseGuards(AuthGuard)
export class ComponentsController {

  constructor(private readonly componentsService: ComponentsService, private configService: ConfigService) { }

  
  @Get()
  @ApiQuery({ name: 'types', required: false, description: 'Component types', example: 'asccp' })
  @ApiQuery({ name: 'tags', required: false, description: 'Component tags', example: 'Noun,BOD' })
  @ApiQuery({ name: 'release', required: false, description: 'connectSpec (OAGIS) release version', example: '10.9.2' })
  @ApiOperation({ summary: 'Return information about connectSpec (OAGIS) components.'
    + ' If the release version parameter is not specified it will return the latest version.' })
  @ApiResponse({ status: 200, description: 'Data retrieved'})
  @ApiResponse({ status: 401, description: 'Unauthorized'})
  @ApiBearerAuth()
  findAll(
    @Query('tags') tags,
    @Query('release') release,
    @Query('types') componentTypes,
  ) {
    return this.componentsService.getAllComponentsMetadata(tags, release, componentTypes);
  }


  @Get('uuid/:uuid')
  @ApiQuery({ name: 'release', required: false, description: 'connectSpec (OAGIS) release version, e.g. release=10.9.2' })
  @ApiOperation({ summary: 'Return component schema for the given UUID -'
  + ' currently XSD is the only schema type supported. Note that if the release version parameter is not specified it will return the latest version.' })
  @ApiResponse({ status: 200, description: 'Data retrieved'})
  @ApiResponse({ status: 401, description: 'Unauthorized'})
  @ApiBearerAuth()
  findComponent(
    @Param('uuid') uuid: string,
    @Query('release') release,
    @Query('schemaType') schemaType
  ) {
      return this.componentsService.getStandaloneComponent(uuid, schemaType, release);
  }

  @Get('releases')
  @ApiOperation({ summary: 'Return description of all connectSpec (OAGIS) releases available in connectCenter (Score)' })
  @ApiResponse({ status: 200, description: 'Data retrieved'})
  @ApiResponse({ status: 401, description: 'Unauthorized'})
  @ApiBearerAuth()
  getReleases() {
    return this.componentsService.getReleases();
  }


  @Get('latest_release')
  @ApiOperation({ summary: 'Return description of the latest connectSpec (OAGIS) release available in connectCenter (Score)' })
  @ApiResponse({ status: 200, description: 'Data retrieved'})
  @ApiResponse({ status: 401, description: 'Unauthorized'})
  @ApiBearerAuth()
  getLocal() {
    return this.componentsService.getLatestRelease();
  }

}

