package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    //쿼리 메소드 기능
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    //네임드쿼리 호출
    //@Query 어노테이션을 선언하지 않아도 메소드명만 맞춰줬다면 호출된다.
    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);


    //@Query 어노테이션 사용
    //컴파일 에러 발생
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username,
                          @Param("age") int age);

    //@Query 결과 값 DTO로 가져오기
    //new 키워드 와 패키지명을 선언해야 한다.
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //파라미터 바인딩 - 컬렉션
    //IN절에 사용
    @Query("select m from Member m where m.username in :names")
    List<Member> findByName(@Param("names") Collection<String> name);

    //페이징 처리
    Page<Member> findByAge(int age, Pageable pageable);

    //페이징 처리 - 카운트 쿼리 지정 가능(조인을 없애 성능을 높임)
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")
    Page<Member> findByAge_count(int age, Pageable pageable);

    //벌크 처리
    //벌크성 쿼리는 영속성 컨텍스트를 거치지 않고 바로 DB로 가기 때문에 실행 후
    //영속성 컨텍스트를 날려줘야 한다.
    //@Modyfing(clearAutomatically = true) 설정하면 자동 clear
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    //지연로딩 - N+1 문제 해결을 위한 페치조인 처리
    //페치조인 이란? 한번에 조인으로 가져오기
    //1. jpql자체에 fetch 기술
    //2. @EntityGrapth 어노테이션, jpql이나 쿼리메소드에도 적용 가능
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    //Hint
    //값을 업데이트 하면 더티체킹이 일어나는데, 변경하지 않을 메소드일 경우 Hint를 선언해서
    //불필요한 더티체킹을 없앨 수 있다.
    @QueryHints(value = @QueryHint(name="org.hibernate.readOnly", value="true"))
    Member findReadOnlyByUsername(String username);
}
