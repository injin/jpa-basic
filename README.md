# jpa-basic
[인프런] 자바 ORM 표준 JPA 프로그래밍 - 기본편

## 1. JPA 소개

### 1.1. SQL 중심적인 개발의 문제점

- 객체와 RDB의 사상이 다르다 (**패러다임의 불일치**) → 객체다운 모델링?
  객체와 관계형 데이터베이스의 차이에서 발생하는 문제
  : 상속, 연관 관계 저장, 객체 그래프 탐색, 비교하기, 엔티티 신뢰문제
  : 연관관계 ⇒ 객체는 참조를 사용, 테이블은 외래키 사용
  : 비교하기 ⇒ 동일한 트랜젝션에서 조회한 엔티티는 같음을 보장
- 진정한 의미의 계층 분할 어렵다. (물리적으로는 객체와 DB가 분리돼 있을지라도 논리적으로는 어렵다)
- 객체답게 모델링 할수록 매핑 작업만 늘어난다.
  ⇒ 객체를 자바 컬렉션에 저장 하듯이 DB에 저장할 수는 없을까?
  ⇒ 그래서 Java진영에서 나온 것이 JPA (구현된 것은 하이버네이트)

### 1.2. JPA란

- ORM이란 객체는 객체대로 설계하고 RDB는 RDB대로 설계해서 중간에 매핑을 해줄 수 있게 하는 기술 ⇒ 패러다임 불일치 해결
- JPA는 애플리케이션과 JDBC 사이에서 동작하며 엔터티를 분석하고 쿼리를 생성하고 JDBC API를 사용
- 초창기 ORM의 자바표준으로 **EJB**가 있었지만 아마추어적이었고 이를 보완하여 만든 ORM 프레임웤이 하이버네이트(오픈소스)이고 이것을 기준으로 다시 자바표준으로 만든 것이 **JPA**임
- JPA는 인터페이스이고 이를 구현한 것이 하이버네이트
- 장점
    - 생산성, 유지보수
    - 패러다임 불일치 4가지 해결 ⇒ 상속, 연관관계, 객체 그래프 탐색, 비교하기
    - 성능 최적화 : `캐싱`, `쓰기 지연`, `지연 로딩`과 `즉시 로딩`

---

## 2. JPA 시작하기

### 2.1. 프로젝트 생성

1) IntelliJ > File > New > Project > Maven > artifactId 등 입력
2) pom.xml에 라이브러리 추가

```xml
<dependencies>
    <!-- JPA 하이버네이트 -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-entitymanager</artifactId>
        <version>5.3.10.Final</version>
    </dependency>
    <!-- H2 데이터베이스 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>1.4.199</version>
    </dependency>
</dependencies>
```

3) /META-INF/persistence.xml 생성
4) Main 클래스 생성 (엔티티 매니저 팩토리)
5) Entity 클래스 생성

---

## 3. 엔티티 매핑

- 객체와 테이블 매핑: `@Entity` , `@Table`
- 필드와 컬럼 매핑: `@Column`
- 기본 키 매핑: `@Id` , `@GeneratedValue` , `@SequenceGenerator`
- 연관관계 매핑: `@ManyToOne` , `@JoinColumn`

```jsx
@Entity
@Table(name = "ORDERS")
public class Order extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;
}
@Entity
public class Member extends BaseEntity {
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
```

<aside>
❗ - `@Enumerated` **에는 ORDINAL사용하지 말고 STRING 타입 사용할 것. 순서 꼬일 위험 있음. 기본값은 ORDINAL이므로 꼭 바꿔준다.**
- IDENTITY 전략은 em.persist() 시점에 즉시 INSERT SQL 실행한다. (commit 시점 아님) 영속성 컨텍스트에서 관리하려면 PK가 우선 필요하기 때문
- SEQUENCE 전략에서는 allocationSize를 지정하여(기본50) 쿼리 횟수를 줄임

</aside>

---

## 4. 엔티티 매핑 기초

- `@ManyToOne` , `@JoinColumn(name = "TEAM_ID")`
- `@OneToMany(mappedBy = "team")` ⇒ 양방향 매핑 시 사용

    <aside>
    ❗ - 외래키가 있는 곳을 연관관계의 주인으로 정해라. DB 테이블로 따져서 N쪽이 주인
    예) N:1 관계의 멤버-팀 테이블 사이에서는 멤버가 연간관계의 주인
    - mappedBy는 읽기전용이기에 set해도 insert되지 않음

    </aside>

- 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자 ⇒ 연관관계 편의 메소드 생성

    ```java
    // 1에 설정한다면
    public void setTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
    // N에 설정한다면
    public void addChild(Child child) {
        childList.add(child);
        child.setParent(this);
    }
    ```


---

## 5. 다양한 연관관계 매핑

`주테이블` , `대상테이블` , `연관관계의 주인` ,

- **다대일 [N:1] 양방향**
  맨 앞에 나온 것(다)이 연관관계의 주인이고 외래키가 위치 (예: 멤버↔팀에서 멤버 엔티티에 외래키 위치) 연관관계의 주인이라는 것은 insert, update 쿼리가 나감
- **일대다 [1:N] 양방향**
  다대일 양방향을 사용하자. (일대다는 설계가 복잡해짐)
- **일대일 [1:1] 단방향**
  주테이블에 외래키가 위치 vs 대상테이블에 외래키 위치
  ⇒ DBA와 협의할 것. (김영한 강사님은 전자를 선호)
- **다대다 [N:N]**
  실무에서 사용하지 말자. 연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야 함

<aside>
❗ **외래키가 있는 곳을 연관관계의 주인으로 정하라.**

</aside>

---

## 6. 고급 매핑

- JPA의 상속관계 매핑은 **슈퍼타입-서브타입 논리모델**을 어떻게 DB로 구현하든 다 지원해줌
  (1) 조인 전략 (2) 단일 테이블 전략 (3) 구현 클래스마다 테이블 전략
  `@Inheritance(strategy = InheritanceType.*JOINED*)` ⇒ 상위 클래스에
  `@DiscriminatorColumn` ⇒ 상위 클래스에
  `@DiscriminatorValue("AL")` ⇒ 하위 클래스에
- **(1) 조인 전략**
  `@Inheritance(strategy = InheritanceType.*JOINED*)`
- **(2) 단일 테이블 전략**
  한 테이블에 전부 입력하고 DTYPE으로 서브타입 구분함. `@DiscriminatorColumn` 없어도 DTYPE 자동으로 생성됨
  `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`
- **(3) 구현 클래스마다 테이블 전략**
  `@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)`
  상위클래스 타입으로 조회할 때, 모든 하위타입 테이블을 조회해 UNION ALL 하므로 비효율적. 사용하지 말 것

<aside>
❗ - **상위 클래스는 abstract 클래스로 만들 것**

</aside>

- `@MappedSuperclass`
  공통 매핑 정보가 필요할 때 사용. 공통 클래스에 어노테이션 추가 & 상속받아 사용
  Auditing에 활용

---

## 7. 프록시와 연관관계 관리

- `em.getReference()`
  em.find와 다르게 DB에 쿼리하지 않고 객체를 조회하는 것
  프록시 객체에 값이 없을 때, 영속성 컨택스트를 통해 초기화(DB쿼리) 요청 후 실제 엔티티 생성
- **지연로딩**
  @ManyToOne, @OneToOne은 기본이 즉시 로딩 ⇒ LAZY로 설정할 것
  @OneToMany, @ManyToMany는 기본이 지연 로딩
  `@ManyToOne(fetch = FetchType.*LAZY*)`
- **즉시로딩**
  `@ManyToOne(fetch = FetchType.EAGER)`

    <aside>
    ❗ **실무에서는 가급적 지연로딩 사용할 것. 즉시로딩은 상상하지 못하는 쿼리가 나간다.
    즉시로딩은 JPQL 사용 시 N+1 발생 (예: EAGER로 설정했는데 select m from Member로 조회 시)**

    </aside>

- **영속성 전이 : CASCADE**
  `@OneToMany(mappedBy = ”parent”, cascade = CascadeType.ALL)`
    - 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속상태로 만들도 싶을 때
      (예: 부모 엔티티 저장할 때 자식 엔티티도 저장)
    - 단일 엔티티에 완전히 종속적일 때만 사용
- **고아객체 : orphanRemoval**
  `@OneToMany(mappedBy = ”parent”, cascade = CascadeType.ALL, orphanRemoval = true)`

    <aside>
    ❗ `CascadeType.REMOVE`와 `orphanRemoval = true` 의 차이

    - 부모 엔티티 삭제
        - `CascadeType.REMOVE`와 `orphanRemoval = true`는 부모 엔티티를 삭제하면 자식 엔티티도 삭제한다.
    - 부모 엔티티에서 자식 엔티티 제거
        - `CascadeType.REMOVE`는 자식 엔티티가 그대로 남아있는 반면, `orphanRemoval = true`는 자식 엔티티를 제거한다.
    </aside>


---

## 8. 값 타입

- **JPA 데이터 타입 분류**
    - 엔티티 타입 : @Entity로 정의하는 객체
    - 값 타입 : int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
      값 타입은 소속된 엔티티에 생명주기를 맡기는 것
- **임베디드 타입**
  새로운 값 타입을 직접 정의. 임베디드 타입을 사용하기 전과 후에 매핑하는 테이블은 같음.
  `@Embeddable`: 값 타입을 정의하는 곳에 표시
  `@Embedded`: 값 타입을 사용하는 곳에 표시
  한 엔티티에서 같은 값 타입을 사용하면? `@AttributeOverrides` , `@AttributeOverride` 사용해서 컬럼명 속성 재정의

    <aside>
    ❗ **값 타입의 실제 인스턴스인 값을 공유하는 것은 위험. 복사해서 사용할 것
    객체 타입의 한계 ⇒ 불변 객체로 설계
    값 타입의 비교는 항상 equals(b)를 사용해서 동등성(equivalance) 비교를 해야 함**

    </aside>

- **값 타입 컬렉션**
  `@ElementCollection`
  `@CollectionTable(name = ”테이블명”)`
    - 값 타입 컬렉션은 전부 지연로딩이 기본임
    - 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장

    ```java
    @ElementCollection
    @CollectionTable(name = "FAVORITE_FOOD", joinColumns =
            @JoinColumn(name = "MEMBER_ID")
    )
    @Column(name = "FOOD_NAME")
    private Set<String> favoriteFoods = new HashSet<>();
    ```


---

## 9. 객체지향 쿼리언어 - 기본문법

- **JPQL** 은 테이블이 아닌 엔티티 객체를 대상으로 쿼리 → 결국 SQL 로 변환됨
- **QueryDSL**은 문자가 아닌 자바코드로 JPQL을 작성할 수 있음. JPQL빌더 역할. 동적쿼리 작성 편리
- **네이티브 SQL**은 JPA가 SQL을 직접 사용하는 기능을 제공하는 것

### 9.1 JPQL 기본 문법과 쿼리 API

- (예시) select m from Member as m where m.age > 18
- 엔티티와 속성은 대소문자 구분O (Member, age)
- JPQL 키워드는 대소문자 구분X (SELECT, FROM, where)
- 엔티티 이름 사용, 테이블 이름이 아님(Member)
- 별칭은 필수(m) (as는 생략가능)
- `TypeQuery` : 반환 타입이 명확할 때 사용
  `Query` : 반환 타입이 명확하지 않을 때 사용
- `query.getResultList()` : 결과가 하나 이상일 때, 리스트 반환
  `query.getSingleResult()` : 결과가 정확히 하나, 단일 객체 반환

### 프로젝션

- SELECT 절에 조회할 대상을 지정하는 것
  엔티티 프로젝션 / 임베디드 타입 프로젝션 / 스칼라 타입 프로젝션
- **여러값 조회**
    - 1. Query 타입으로 조회
    - 2. Object[] 타입으로 조회
    - 3. new 명령어로 조회

      ```java
      SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM Member m
      ```

- **페이징**
  `setFirstResult()` , `setMaxResults()`

    ```java
    String jpql = "select m from Member m order by m.name desc";
    List<Member> resultList = em.createQuery(jpql, Member.class)
    		.setFirstResult(10)
    		.setMaxResults(20)
    		.getResultList();
    ```

- **조인**
    - (예제)

        ```java
        // 내부조인
        SELECT m FROM Member m [INNER] JOIN m.team t
        // 외부조인
        SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
        // 세타조인 : 카타시안곱
        select count(m) from Member m, Team t where m.username = t.name
        // 연관관계 없는 엔티티 외부조인
        SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
        ```

- **서브쿼리**
  **FROM 절의 서브 쿼리는 현재 JPQL에서 불가능** → JOIN으로 풀 수 있으면 풀어서 해결
    - (예제)

        ```java
        // 나이가 평균보다 많은 회원
        select m from Member m where m.age > (select avg(m2.age) from Member m2)
        // 한 건이라도 주문한 고객
        select m from Member m where (select count(o) from Order o where m = o.member) > 0
        // 팀A 소속인 회원
        select m from Member m where exists (select t from m.team t where t.name = ‘팀A')
        // 전체 상품 각각의 재고보다 주문량이 많은 주문들
        select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)
        // 어떤 팀이든 팀에 소속된 회원
        select m from Member m where m.team = ANY (select t from Team t)
        ```

- **조건식(CASE 등등)**
- **JPQL 함수**

---

## 10. 객체지향 쿼리언어 - 중급문법

### 10.1 페치조인

- SQL 조인 종류X 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능
- select m from Member m join **fetch** m.team
  ⇒ SELECT M.**, T.** FROM MEMBER M
  INNER JOIN TEAM T ON M.TEAM_ID=T.ID
- 컬랙션 패치조인 시 다건 출력되는 문제 해결하기 위해 **DISTINCT** 키워드 사용

### 10.2 페치 조인의 특징과 한계

- 페치 조인 대상에는 별칭을 줄 수 없음
- 둘 이상의 컬렉션은 페치 조인 할 수 없음
- 컬렉션을 페치 조인 하면 페이징 API 사용 불가 (일대일, 다대일은 가능)
  ⇒ 굳이 하려면 다쪽 엔티티를 먼저 조회 후 다쪽을 한번에 LAZY 로딩
  ⇒ (방법1)  `@BatchSize(size= 100)`
  ⇒ (방법2) `<property name="hibernate.default_batch_fetch_size" value="100" />`

### 10.3 다형성 쿼리

- **TYPE** : 조회 대상을 특정 자식으로 한정
  `select i from Item i where type(i) IN (Book, Movie)`
- **TREAT** : 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용
  `select i from Item i where treat(i as Book).auther = ‘kim’`

### 10.4 엔티티 직접 사용 - 기본키,외래키

### 10.5 Named 쿼리

- `@NamedQuery` 어노테이션 또는 XML에 정의하여 사용. 쿼리 미리 정의해 두고 이름 부여. 재사용.
- 정적쿼리만 사용 가능. 애플리케이션 로딩 시점에 검증하고 초기화 후 재사용.

### 10.6 벌크연산

- 쿼리 한 번으로 여러 테이블의 로우 변경
- `em.createQuery("update문").executeUpdate()` 사용

    <aside>
    ❗ **벌크연산 수행 후 영속성 컨텍스트 초기화 할 것. flush는 돼서 DB에는 반영됐으나 영속성 컨텍스트 sync 안맞을 수 있음**

    </aside>


---

## ○ 요약

- `EntityManagerFactory`는 서버가 올라올 때 DB당 하나만 생성. `EntityManager`는 트랜잭션 당 하나만 생성, 사용 후 버려야 함 (쓰레드간 공유해선 안됨)
- 트랜잭션이 같으면 같은 EntityManager를 사용. Repository 빈이 달라도 EntityManager는 같은 것을 사용
- JPA 표준 스펙상 프록시 때문에 엔티티 클래스에는 No Argument 기본 생성자가 하나 있어야함 ⇒ protected로 작성하거나 `@NoArgsConstructor(access = AccessLevel.*PROTECTED*)`
- JPQL은 엔티티 객체를 대상으로 쿼리하기 때문에 방언을 바꿔도 소스를 변경하지 않아도 됨.
  ↔ SQL은 데이터베이스 테이블을 대상으로 쿼리하기 때문에 DB에 종속적임
- EntityManager 를 통해 `**영속성 컨텍스트**`에 접근한다.
  영속성 컨텍스트는 “엔티티를 영구 저장하는 환경”으로 논리적인 개념임
  영속 상태라고 해서 DB에 쿼리된 것이 아니라 트랜잭션 커밋 해야만 쿼리됨
- `**변경감지(Dirty Checking)**` 기능이 있기에 조회한 엔티티를 변경해도 다시 persist 호출 할 필요 없음
- 실무에서 글로벌 로딩 전략은 모두 지연로딩. 최적화가 필요한 곳은 페치 조인 적용. 그러면 대부분의 문제인 N+1문제 해결됨
- 묵시적 조인 사용하지 말고 명시적 조인 사용해라. SQL 파악이 어려워 쿼리 튜닝 등 어려움.
- setter를 불필요하게 열어두면 어느 타이밍에 UPDATE되는 지 추적 어려움. 대신에 변경용 비즈니스 메서드를 제공할 것 (예: 값 타입 클래스에서 setter 없애고 생성자로만 값 세팅하게 설계. immutable)
- 동적 쿼리를 생성하는 데 JPA 표준 스펙으로 JPA Criteria가 있지만 사용하기 복잡.
- **준영속 엔티티를 수정하는 2가지 방법**
- 변경 감지 기능 사용
- 병합(merge) 사용
* 준영속 엔티티는 영속성 컨텍스트가 더이상 관리하지 않는 엔티티
* 병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능

    <aside>
    📌 **merge는 데이터 업데이트 하기 위해 쓰지 말 것. 영속성 상태를 벗어난 엔티티를 영속 상태로 만들기 위한 용도로 사용**

    </aside>

- application.yml 설정이 없으면 기본적으로 h2 DB를 메모리 모드로 실행
