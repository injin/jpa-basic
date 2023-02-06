package jpql;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMainFetchJoin {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3= new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();


            String query = "select m from Member m";
            List<Member> result = em.createQuery(query).getResultList();
            for (Member member : result) {
                System.out.println("member = " + member.getUsername() + " , " + member.getTeam().getName());
                // 회원1, 팀A (SQL)
                // 회원2, 팀A (1차캐시)
                // 회원3, 팀B (SQL)
                // 회원100명 -> 최대 N+1 쿼리 (즉시로딩, 지연로딩 모두)
            }


            // 패치조인
            String fetchQuery = "select m from Member m join fetch m.team";
            List<Member> fetchResult = em.createQuery(query).getResultList();
            for (Member member : fetchResult) {
                System.out.println("member = " + member.getUsername() + " , " + member.getTeam().getName()); // 이 때 Team은 프록시가 아님
            }

            // 컬렉션 패치조인
            String collectionFetchQeury = "select t from Team t join fetch t.members where t.name = '팀A'";
            List<Team> teams = em.createQuery(collectionFetchQeury).getResultList();
            for (Team team : teams) { // 팀A가 2줄 출력 (DB에서 그렇게 가져왔기 때문)
                System.out.println("teamName : " + team.getName() + " members : " + team.getMembers().size());
            }

            // 패치조인과 DISTINCT
            String distinctQuery = "select t from Team t join fetch t.members where t.name = '팀A'";
            List<Team> distinctTeams = em.createQuery(collectionFetchQeury).getResultList();
            for (Team distinctTeam : distinctTeams) {
                System.out.println("teamName : " + distinctTeam.getName() + " members : " + distinctTeam.getMembers().size());
                for (Member member : distinctTeam.getMembers()) {
                    System.out.println("--> member = " + member);
                }
            }

            // Named 쿼리
            List<Member> resultList = em.createNamedQuery("Member.findUsername", Member.class).setParameter("username", "회원1").getResultList();
            for (Member member : resultList) {
                System.out.println("member = " + member);
            }

            // 벌크연산
            // (예시: 모든 회원의 나이를 한 번에 20살로 없데이트)
            int count = em.createQuery("update Member m set m.age = 20").executeUpdate();
            System.out.println("updat count = " + count);
            em.clear(); // 중요!!

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
