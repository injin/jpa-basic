package valueType;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setName("aaa");
            member.setHomeAddress(new Address("homeCity", "street", "zipcode"));
            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("족발");

            // (1) 값타입 컬렉션으로 구현
            member.getAddressHistory().add(new Address("old1", "street", "zipcode"));
            member.getAddressHistory().add(new Address("old2", "street", "zipcode"));


            // (2) 일대다 엔티티로 구현
            member.getAddressEntityList().add(new AddressEntity("old1", "street", "zipcode"));
            member.getAddressEntityList().add(new AddressEntity("old2", "street", "zipcode"));
            em.persist(member);

            System.out.println("============ 값 타입 수정 삭제 ============");
            Member findMember = em.find(Member.class, member.getId());

            // 수정 homeCity -> newCity
            //findMember.getHomeAddress().setCity("newCity");
            Address address = findMember.getHomeAddress();
            findMember.setHomeAddress(new Address("newCity", address.getStreet(), address.getZipcode()));

            findMember.getFavoriteFoods().remove("치킨");
            findMember.getFavoriteFoods().add("한식");

            findMember.getAddressHistory().remove(new Address("old1", "street", "zipcode")); // 내부적으로 equals 비교
            findMember.getAddressHistory().add(new Address("newCity1", "street", "zipcode"));



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
