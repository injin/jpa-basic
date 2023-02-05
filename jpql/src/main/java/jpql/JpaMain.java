package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            member.setType(UserType.ADMIN);
            em.persist(member);

            em.flush();
            em.clear();

            // getSingleResult 예시
            Member result = em.createQuery("select m from Member m where m.username = :username", Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();
            System.out.println("result == " + result.getUsername());

            // 프로젝션 예시 : Object[] 타입으로 조회
            List<Object[]> resultList = em.createQuery("select m.username, m.age from Member m").getResultList();
            Object[] pResult = resultList.get(0);
            System.out.println("username = " + pResult[0]);
            System.out.println("age = " + pResult[1]);

            // 프로젝션 예시 : new 명령어로 조회
            List<MemberDTO> resultListNew = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class).getResultList();
            MemberDTO nResult = resultListNew.get(0);
            System.out.println("username = " + nResult.getUsername());
            System.out.println("age = " + nResult.getAge());

            // JPQL 타입 표현식
            List<Object[]> resultListType = em.createQuery("select m.username, 'HELLO', true from Member m where m.type = :userType")
                            .setParameter("userType", UserType.ADMIN)
                            .getResultList();
            for (Object[] objects : resultListType) {
                System.out.println("objects == " + objects[0]);
                System.out.println("objects == " + objects[1]);
                System.out.println("objects == " + objects[2]);
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();

    }
}
