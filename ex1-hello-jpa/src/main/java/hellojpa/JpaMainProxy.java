package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMainProxy {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            /*
            // [1.등록]
            Member member = new Member();
            member.setId(2L);
            member.setName("helloB");
            em.persist(member);
            // [2.단건 조회]
            Member findMember = em.find(Member.class, 1L);
            // [3.페이징 조회]
            List<Member> memberList = em.createQuery("select m from Member as m", Member.class)
                    .setFirstResult(0)
                    .setMaxResults(8)
                    .getResultList();
            for (Member member : memberList) {
                System.out.println("member.name = " + member.getName());
            }
            // [4.수정]
            findMember.setName("HelloJPA");
            */


            // (샘플1) 프록시
            Member member1 = new Member();
            member1.setName("member1");
            em.persist(member1);
            Member member2 = new Member();
            member2.setName("member2");
            em.persist(member2);

            em.flush();
            em.clear();

            Member refMember = em.getReference(Member.class, member2.getId());
            //System.out.println("findMember == " + refMember.getName()); // 강제초기화
            Member findMember = em.find(Member.class, member1.getId());


            System.out.println("findMember == refMember" + (findMember.getClass() == refMember.getClass())); // 실제는 프록시 클래스
            System.out.println("refMember instanceof " + (refMember instanceof Member));


            // (샘플2) 즉시로딩, 지연로딩
            Team team = new Team();
            team.setName("team3");
            em.persist(team);

            Member member3 = new Member();
            member3.setName("member3");
            member3.setTeam(team);
            em.persist(member3);

            em.flush();
            em.clear();

            Member findMember3 = em.find(Member.class, member3.getId());
            System.out.println("======");
            System.out.println(findMember3.getTeam().getName()); // 지연로딩
            System.out.println("======");





            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();

    }
}
