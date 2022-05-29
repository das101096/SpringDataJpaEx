package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Disabled
    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    }

    @Disabled
    @Test
    public void basicTest() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member byId1 = memberRepository.findById(member1.getId()).get();
        Member byId2 = memberRepository.findById(member2.getId()).get();
        Assertions.assertThat(byId1).isEqualTo(member1);
        Assertions.assertThat(byId2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        Assertions.assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long count1 = memberRepository.count();
        Assertions.assertThat(count1).isEqualTo(0);
    }

    //쿼리메소드 테스트
    @Disabled
    @Test
    public void findbyUsername() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> aaa = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        Assertions.assertThat(aaa.get(0).getAge()).isEqualTo(20);
        Assertions.assertThat(aaa.size()).isEqualTo(1);
    }

    //네임드쿼리 테스트
    @Disabled
    @Test
    public void namedQuery() {
        Member member = new Member("회원1");
        memberRepository.save(member);

        List<Member> findmember = memberRepository.findByUsername("회원1");
        Assertions.assertThat(findmember.get(0).getUsername()).isEqualTo("회원1");
    }

    //@Query 어노테이션 테스트
    @Disabled
    @Test
    public void QueryTest() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> aaa = memberRepository.findUser("AAA", 10);
        Assertions.assertThat(aaa.get(0).getAge()).isEqualTo(10);
    }

    //@Query DTO로 가져오기 테스트
    @Disabled
    @Test
    public void QueryDtoTest1() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        Assertions.assertThat(usernameList.get(0)).isEqualTo("AAA");
    }

    @Disabled
    @Test
    public void QueryDtoTest2() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member1 = new Member("AAA", 10);
        member1.setTeam(team);
        memberRepository.save(member1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto m : memberDto) {
            System.out.println(m);
        }
    }

    //파라미터 바인딩 - 컬렉션 테스트
    @Disabled
    @Test
    public void ParameterTest() {
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> byName = memberRepository.findByName(Arrays.asList("aa", "bb", "cc","AAA"));
        for (Member member : byName) {
            System.out.println(member);
        }
    }

    //Spring Data JPA 페이징 테스트
    //PageRequest를 생성해야 한다.
    @Disabled
    @Test
    public void paging() {
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));
        memberRepository.save(new Member("member6",10));
        memberRepository.save(new Member("member7",10));

        int age = 10;
        int offset = 0;
        int limit = 3;

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

        //when
        //API로 반환하면 안됨
        Page<Member> byAge = memberRepository.findByAge(age, pageRequest);

        //DTO로 반환할 수 있도록 변경 (실무)
        //DTO로 변경하면 API로 반환 가능
        Page<MemberDto> map = byAge.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //then
        List<Member> content = byAge.getContent();
        long totalElements = byAge.getTotalElements();

        for (Member member : content) {
            System.out.println("member : " + member);
        }
        System.out.println("totalElements : " + totalElements);
    }

    //벌크성 쿼리 테스트
    @Disabled
    @Test
    public void bulk() {
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",22));

        //when
        int i = memberRepository.bulkAgePlus(20);
        em.flush();
        em.clear();

        List<Member> result = memberRepository.findByUsername("member5");
        System.out.println(result.get(0));

        //then
        Assertions.assertThat(i).isEqualTo(3);
    }

    //지연로딩 테스트
    //Team 호출 시 프록시를 띄워 검색
    @Test
    public void findMemberLazy(){
        //given
        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member1",10,teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();
        
        //when
        //지연 로딩 N+1 문제 발생
        //쿼리 N번 날리면 N개 쿼리를 프록시로 더 실행해야함
        List<Member> all = memberRepository.findAll();
        for (Member member : all) {
            System.out.println(member);
        }
    }

    //Hint 테스트
    @Test
    public void queryHint() {
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member member = memberRepository.findById(member1.getId()).get();
        member.setUsername("member2");

        em.flush();
    }

    //사용자 정의 리파지토리 테스트
    @Test
    public void callcustom() {
        List<Member> memberCustom = memberRepository.findMemberCustom();
    }
}