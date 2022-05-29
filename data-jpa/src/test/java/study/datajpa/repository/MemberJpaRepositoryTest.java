package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(savedMember.getId());

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    }


    @Test
    public void basicTest() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        Member byId1 = memberJpaRepository.findById(member1.getId()).get();
        Member byId2 = memberJpaRepository.findById(member2.getId()).get();
        Assertions.assertThat(byId1).isEqualTo(member1);
        Assertions.assertThat(byId2).isEqualTo(member2);

        List<Member> all = memberJpaRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(2);

        long count = memberJpaRepository.count();
        Assertions.assertThat(count).isEqualTo(2);

        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long count1 = memberJpaRepository.count();
        Assertions.assertThat(count1).isEqualTo(0);
    }

    @Test
    public void findbyUsername() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<Member> aaa = memberJpaRepository.findByUserNameAndAgeGreaterThen("AAA", 15);
        Assertions.assertThat(aaa.get(0).getAge()).isEqualTo(20);
        Assertions.assertThat(aaa.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        Member member = new Member("회원1");
        memberJpaRepository.save(member);

        List<Member> findMembers = memberJpaRepository.findByUsername("회원1");
        Assertions.assertThat(findMembers.get(0).getUsername()).isEqualTo("회원1");

    }

    //순수 JPA 정렬 페이징 테스트
    @Test
    public void paging() {
        memberJpaRepository.save(new Member("member1",10));
        memberJpaRepository.save(new Member("member2",10));
        memberJpaRepository.save(new Member("member3",10));
        memberJpaRepository.save(new Member("member4",10));
        memberJpaRepository.save(new Member("member5",10));
        memberJpaRepository.save(new Member("member6",10));
        memberJpaRepository.save(new Member("member7",10));

        int age = 10;
        int offset = 0;
        int limit = 3;

        List<Member> byPage = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCont(age);

        for (Member member : byPage) {
            System.out.println(member);
        }
    }

    //벌크성 쿼리 테스트
    @Test
    public void bulk() {
        //given
        memberJpaRepository.save(new Member("member1",10));
        memberJpaRepository.save(new Member("member2",19));
        memberJpaRepository.save(new Member("member3",20));
        memberJpaRepository.save(new Member("member4",21));
        memberJpaRepository.save(new Member("member5",22));

        //when
        int i = memberJpaRepository.bulkAgePlus(20);

        //then
        Assertions.assertThat(i).isEqualTo(3);
    }
}