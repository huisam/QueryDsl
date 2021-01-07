# QueryDsl

* QueryDsl을 신나게 배워보자!😄

## gradle 설정 방법

1. queryDSL 관련 plugin을 가져오고,
2. dependencies 에서 query dsl 을 `implementation`
3. 그 다음에 queryDsl로 compile 해서 나온 java 파일에 대한 타겟 디렉토리 명시
4. compileQueryDsl에 대한 gradle 명시 -> 작업은 **annotationProcessor** 로 진행

```groovy
// plugin 설정
plugins {
    id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
}

// dependency 설정 추가
dependencies {
    implementation 'com.querydsl:querydsl-jpa'
}

// build 디렉토리 설정
def querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

// 빌드하면 타겟 디렉토리 지정
sourceSets {
    main.java.srcDir querydslDir
}

configurations {
    querydsl.extendsFrom compileClasspath
}

compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}
```

## QueryDsl 사용 방법

일반적으로 `Repository` 안에서 **JPAQueryFactory** 를 가져와서 사용하게 된다

```java
@Repository
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }
}
```

`jpa` 에서 지원하는 **EntityManager**를 의존성 주입받고,  
**JPAQueryFactory** 객체를 만들어줘서 QueryDsl을 정상적으로 사용할 수 있게 된다!

그래서 userName 을 조건으로 찾아오는 쿼리를 만들면?

```java
public List<Member> findByUserName_QueryDsl(String userName) {
        return queryFactory
                .selectFrom(QMember.member)
                .where(member.username.eq(userName))
                .fetch();
    }
```

보통 QueryDsl로 인해서 생성된 entity는 `Q` 가 붙어서 객체가 생성되게 된다!

이게 놀라운 것이 뭐냐면, 일반적으로 `jpaNativeQuery` = `@Query` 는   
런타임시에 쿼리가 오류인지 아닌지 알게되는데,  
**queryDSL**의 경우에는 컴파일 타임에 알 수 있어서 쿼리를 작성함에 있어서 굉장한 장점을 가진다!

심지어 `where` 절에 존재하는 조건문도 **메서드** 로 추출하여,  
언제든지 다시 재사용할 수 있다는 장점도 있다!

```java
private BooleanExpression userNameEq(String userName) {
        return hasText(userName)?member.username.eq(userName):null;
}
```

