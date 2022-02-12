package hello.hellospring.repository;


import hello.hellospring.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.*;

public class MemoryMemberRepository implements MemberRepository{

    private static Map<Long, Member> store = new HashMap<>();
    private static long seqeunce = 0L;

    @Override
    public Member save(Member member) {
         member.setId(++seqeunce);
         store.put(member.getId(), member);
         return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        // null이 반환된 가능성이 있으면 감싸줌
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore(){
        store.clear();
    }
}
