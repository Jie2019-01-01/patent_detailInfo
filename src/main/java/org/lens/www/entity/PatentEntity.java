package org.lens.www.entity;

/**
 * 模拟model
 */
public class PatentEntity {

    private String ab;
    private String cites_by_parents;
    private String cites_parents;
    private String family_info;

    public PatentEntity(){System.out.println("* 模型类已经就绪.");}
    public PatentEntity(String ab, String cites_by_parents, String cites_parents, String family_info) {
        this.ab = ab;
        this.cites_by_parents = cites_by_parents;
        this.cites_parents = cites_parents;
        this.family_info = family_info;
    }

    public String getAb() {
        return ab;
    }

    public void setAb(String ab) {
        this.ab = ab;
    }

    public String getCites_by_parents() {
        return cites_by_parents;
    }

    public void setCites_by_parents(String cites_by_parents) {
        this.cites_by_parents = cites_by_parents;
    }

    public String getCites_parents() {
        return cites_parents;
    }

    public void setCites_parents(String cites_parents) {
        this.cites_parents = cites_parents;
    }

    public String getFamily_info() {
        return family_info;
    }

    public void setFamily_info(String family_info) {
        this.family_info = family_info;
    }

    @Override
    public String toString() {
        return "PatentEntity{" +
                "ab='" + ab + '\'' +
                ", cites_by_parents='" + cites_by_parents + '\'' +
                ", cites_parents='" + cites_parents + '\'' +
                ", family_info='" + family_info + '\'' +
                '}';
    }
}
