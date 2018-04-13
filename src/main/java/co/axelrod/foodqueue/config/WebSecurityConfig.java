package co.axelrod.foodqueue.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import java.util.Collections;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Value("${ldap.domain}")
	private String ldapDomain;

	@Value("${ldap.url}")
	private String ldapUrl;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.anyRequest().fullyAuthenticated()
				.and()
			.formLogin();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(Collections.singletonList(activeDirectoryLdapAuthenticationProvider()));
	}
	@Bean
	public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
		ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(ldapDomain, ldapUrl);
		provider.setConvertSubErrorCodesToExceptions(true);
		provider.setUseAuthenticationRequestCredentials(true);
		return provider;
	}
}
