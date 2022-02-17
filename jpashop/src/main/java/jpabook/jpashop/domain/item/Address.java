package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // Embedded 타입의 경우 public, protected 두 가지 중 하나로 설정해야 하기 때문에
    // 그나마 안전한 protected 생성자를 사용한다
    protected Address(){

    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
