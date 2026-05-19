package com.cricket.config;

import java.util.Properties;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.cricket.util.CricketUtil;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

	public static DriverManagerDataSource dataSource;
	
    @Bean
    public DataSource dataSource() {
    	if(dataSource == null) {
    		dataSource = new DriverManagerDataSource();
    		dataSource.setDriverClassName("net.ucanaccess.jdbc.UcanaccessDriver");
    		dataSource.setUrl("jdbc:ucanaccess://" + CricketUtil.CRICKET_DIRECTORY 
            	+ CricketUtil.DATABASE_DIRECTORY + CricketUtil.CRICKET_TEAMS_MDB);
    	}
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("com.cricket.model");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.archive.autodetection","class");
        emf.setJpaProperties(props);
        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
    
    public static void switchDatabase(String cricketDirectory) {
    	String baseDirectory = CricketUtil.CRICKET_DIRECTORY;
    	if(baseDirectory.endsWith("/") || baseDirectory.endsWith("\\")) {
    		baseDirectory = baseDirectory.substring(0, baseDirectory.length() - 1);
    	}
    	
    	String databasePath = "jdbc:ucanaccess://" + baseDirectory;
    	
    	if(cricketDirectory != null && !cricketDirectory.trim().isEmpty()) {
    		databasePath += cricketDirectory.trim();
    	}

    	databasePath += "/" + CricketUtil.DATABASE_DIRECTORY;
    	if(!databasePath.endsWith("/")) {
    		databasePath += "/";
    	}
    	databasePath += CricketUtil.CRICKET_TEAMS_MDB;
    	dataSource.setUrl(databasePath);
    	System.out.println("DATABASE SWITCHED TO = " + databasePath);
    } 
}

//package com.cricket.config;
//
//import java.util.Properties;
//
//import jakarta.annotation.Resource;
//import jakarta.persistence.EntityManagerFactory;
//import javax.sql.DataSource;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
//import org.springframework.core.env.Environment;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//@Configuration
//@EnableTransactionManagement
//@PropertySource("classpath:db.properties")
//public class DataSourceConfig {
//
//    private static final String PROPERTY_NAME_DATABASE_DRIVER = "hibernate.connection.driver_class";
//    private static final String PROPERTY_NAME_DATABASE_URL = "hibernate.connection.url";
//
//    @Resource
//    private Environment env;
//
//    @Bean
//    public DataSource dataSource() {
//        DriverManagerDataSource ds = new DriverManagerDataSource();
//        ds.setDriverClassName(env.getRequiredProperty(PROPERTY_NAME_DATABASE_DRIVER));
//        ds.setUrl(env.getRequiredProperty(PROPERTY_NAME_DATABASE_URL));
//        return ds;
//    }
//
//    @Bean
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//
//        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
//
//        emf.setDataSource(dataSource());
//        emf.setPackagesToScan("com.cricket.model");
//        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//
//        Properties props = new Properties();
//        props.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
//        props.put("hibernate.show_sql", "true");
//        props.put("hibernate.hbm2ddl.auto", "none");
//
//        emf.setJpaProperties(props);
//
//        return emf;
//    }
//
//    @Bean
//    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
//        return new JpaTransactionManager(emf);
//    }
//
//    @Bean
//    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
//        return new PropertySourcesPlaceholderConfigurer();
//    }
//}
