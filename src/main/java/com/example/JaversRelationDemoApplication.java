package com.example;

import org.javers.core.Javers;
import org.javers.hibernate.integration.HibernateUnproxyObjectAccessHook;
import org.javers.repository.sql.ConnectionProvider;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.aspect.JaversAuditableRepositoryAspect;
import org.javers.spring.jpa.JpaHibernateConnectionProvider;
import org.javers.spring.jpa.TransactionalJaversBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class JaversRelationDemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JaversRelationDemoApplication.class, args);
    }

    // javers

    @Bean
    public Javers javers() {
        JaversSqlRepository sqlRepository = SqlRepositoryBuilder
                .sqlRepository()
                .withConnectionProvider(jpaConnectionProvider())
                .withDialect(DialectName.H2)
                .build();

        return TransactionalJaversBuilder
                .javers()
                .withObjectAccessHook(new HibernateUnproxyObjectAccessHook())
                .withNewObjectsSnapshot(true)
                .registerJaversRepository(sqlRepository)
                .build();
    }

    @Bean
    public JaversAuditableRepositoryAspect javersAuditableRepositoryAspect() {
        return new JaversAuditableRepositoryAspect(javers(), authorProvider());
    }

    @Bean
    public ConnectionProvider jpaConnectionProvider() {
        return new JpaHibernateConnectionProvider();
    }

    @Bean
    public AuthorProvider authorProvider() {
        return () -> "test";
    }

    // test code

    @Component
    public static class JaversTest implements CommandLineRunner {

        @Autowired
        private ParentRepository parentRepository;

        @Override
        public void run(String... args) throws Exception {
            Child child = Child.builder().name("child").build();
            List<Child> children = new ArrayList<>();
            children.add(child);
            Parent parent = Parent.builder()
                    .name("parent")
                    .children(children)
                    .build();

            // save new parent + child => works
            Long parentId = parentRepository.save(parent).getId();

            Parent save = parentRepository.findOne(parentId);
            save.getChildren().add(Child.builder().name("child2").build());

            // save existing parent with new child => works with jpa but not with javers enabled
            parentRepository.save(save);
        }
    }
}
