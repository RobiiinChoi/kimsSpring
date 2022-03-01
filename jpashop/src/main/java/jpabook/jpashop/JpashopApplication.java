package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5Module hibernate5Module(){
		Hibernate5Module hibernate5Module = new Hibernate5Module();

		// 강제 지연로딩 설정
		// 이 옵션을 키면 order > memebr , memebr > order 양방향을 계속 로딩하기 때문에 JsonIgnore 옵션을 한 곳에 주어야 한다.
		// 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭 한곳을 jsonIgnore 처리 해야지, 안그러면 양쪽을 서로 호출하면서 무한루프
		// 간단한 어플리케이션이 아니면 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다. 따라서 DTO로 변환 후 반환하는 것이 더 좋다
		// LAZY로딩을 피하기 위해 EAGER로딩으로 설정하면 안됨. 즉시 로딩때문에 연관관계가 필요 없는 경우에도, 데이터를 항상 조회해서
		// 성능 문제가 발생할 수 있다. 즉시 로딩으로 설정 시 성능 튜닝이 어렵기 때문에, 항상 지연 로딩을 기본으로, 성능 최적화가 필요할 경우
		// fetch join 사용
		// hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}

}
