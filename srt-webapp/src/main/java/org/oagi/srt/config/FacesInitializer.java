package org.oagi.srt.config;

import org.apache.myfaces.webapp.StartupServletContextListener;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.oagi.srt.scope.ViewScope;
import org.oagi.srt.web.filter.CharacterEncodingFilter;
import org.primefaces.webapp.filter.FileUploadFilter;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.context.embedded.*;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.faces.application.ViewExpiredException;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@EnableWebMvc
@Configuration
public class FacesInitializer extends WebMvcConfigurerAdapter
        implements EmbeddedServletContainerCustomizer, ServletContextInitializer {

    @Bean
    public ServletListenerRegistrationBean<StartupServletContextListener> startupServletContextListener() {
        return new ServletListenerRegistrationBean(new StartupServletContextListener());
    }

    @Bean
    public ServletRegistrationBean facesServlet() {
        FacesServlet servlet = new FacesServlet();
        ServletRegistrationBean facesServlet = new ServletRegistrationBean(servlet, "*.xhtml");
        facesServlet.setName("Faces Servlet");
        facesServlet.setLoadOnStartup(1);
        return facesServlet;
    }

    @Bean
    public FilterRegistrationBean characterEncodingFilter() {
        FilterRegistrationBean characterEncodingFilter =
                new FilterRegistrationBean(new CharacterEncodingFilter(), facesServlet());
        characterEncodingFilter.setName("Character Encoding Filter");
        return characterEncodingFilter;
    }

    @Bean
    public FilterRegistrationBean fileUploadFilter() {
        FilterRegistrationBean fileUploadFilter = new FilterRegistrationBean(new FileUploadFilter(), facesServlet());
        fileUploadFilter.setName("PrimeFaces FileUpload Filter");
        return fileUploadFilter;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/index.xhtml");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        super.addViewControllers(registry);
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);

        mappings.add("ttf", "application/font-sfnt");
        mappings.add("woff", "application/font-woff");
        mappings.add("eot", "application/vnd.ms-fontobject");
        mappings.add("eot?#iefix", "application/vnd.ms-fontobject");
        mappings.add("svg", "image/svg+xml");
        mappings.add("svg#exosemibold", "image/svg+xml");
        mappings.add("svg#exobolditalic", "image/svg+xml");
        mappings.add("svg#exomedium", "image/svg+xml");
        mappings.add("svg#exoregular", "image/svg+xml");

        container.setMimeMappings(mappings);

        ErrorPage throwable = new ErrorPage(Throwable.class, "/error/error.xhtml");
        ErrorPage viewExpiredException = new ErrorPage(ViewExpiredException.class, "/error/viewExpired.xhtml");
        ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/error/404.xhtml");

        container.addErrorPages(throwable, viewExpiredException, error404Page);

        if (container instanceof TomcatEmbeddedServletContainerFactory) {
            TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory =
                    (TomcatEmbeddedServletContainerFactory) container;

            tomcatEmbeddedServletContainerFactory.addContextCustomizers(context -> {
                LoginConfig config = new LoginConfig();
                config.setAuthMethod("FORM");
                config.setLoginPage("/login.xhtml");
                config.setErrorPage("/login_error.xhtml");
                context.setLoginConfig(config);

                context.addSecurityRole("admin");

                SecurityConstraint constraint = new SecurityConstraint();
                constraint.addAuthRole("admin");

                SecurityCollection collection = new SecurityCollection();
                collection.setName("Admin");
                collection.addPattern("/admin/*");
                constraint.addCollection(collection);

                context.addConstraint(constraint);
            });
        }
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("javax.faces.STATE_SAVING_METHOD", "client");

        servletContext.setInitParameter("primefaces.PRIVATE_CAPTCHA_KEY", "6LfwZwoAAAAAAEhRyntKF1PBzysAJLzqp2v-GMRR");
        servletContext.setInitParameter("primefaces.PUBLIC_CAPTCHA_KEY", "6LfwZwoAAAAAAI-oUHpdvRnkMfu9fXQHxc0P7IBu");
        servletContext.setInitParameter("primefaces.CLIENT_SIDE_VALIDATION", Boolean.TRUE.toString());
        servletContext.setInitParameter("primefaces.UPLOADER", "commons");
        servletContext.setInitParameter("primefaces.THEME", "home");

        servletContext.setAttribute("org.apache.myfaces.DYNAMICALLY_ADDED_FACES_SERVLET", true);
    }

    @Bean
    public CustomScopeConfigurer viewScope() {
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        customScopeConfigurer.addScope("view", new ViewScope());
        return customScopeConfigurer;
    }
}
