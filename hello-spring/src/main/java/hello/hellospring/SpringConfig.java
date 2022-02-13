package hello.hellospring;

import hello.hellospring.aop.TimeTraceApp;
import hello.hellospring.repository.*;
import hello.hellospring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

@Configuration
public class SpringConfig {
//    JPA EntityManager로 설정
//    private EntityManager em;
//
//    @Autowired
//    public SpringConfig(EntityManager em) {
//        this.em = em;
//    }

//    JdbcTemplate으로 설정
//    private final DataSource dataSource;
//
//    @Autowired
//    public SpringConfig(DataSource dataSource){
//        this.dataSource = dataSource;
//    }

    private final MemberRepository memberRepository;

    @Autowired
    public SpringConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository);
    }

//    @Bean
//    public MemberRepository memberRepository() {
//        return new MemoryMemberRepository();
//        return new JdbcMemberRepository(dataSource);
//        return new JdbcTemplateMemberRepository(dataSource);
//        return new JpaMemberRepository(em);
//    }


    // AOP 같은 경우, Config에 등록해서 쓰는게 알아보기 더 쉽기 때문에 선호
//    @Bean
//    public TimeTraceApp timeTraceApp(){
//        return new TimeTraceApp();
//    }

}
