package hello.hellospring.domain;

import javax.persistence.*;

// JPA가 관리하는 엔티티
@Entity
public class Member {

    // 기본키 생성을 DB에 위임
    // Identity 전략은 EntityManager.persist() 시점에 즉시 insert 실행 후 db에서 식별자 조회
    // JPA의 경우 보통 트랜잭션 commit 시점에 insert 실행되나, identity의 경우 커밋 전 persist() 시점에 인서트 실행
    // 그 이유는 JPA가 영속성 컨텍스트에 persist()하는 시점에 구분할 수 있는 식별자가 필요한데, identity 전략의 경우
    // 인서트 하기 전까지 식별자를 할당할 수 없으므로 (자동으로 해주기때문에 db에 넣어봐야 알 수 있음)
    // insert를 persist()시점에 진행하며 할당받은 값을 식별자로 사용한다.
    // https://dololak.tistory.com/464
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
