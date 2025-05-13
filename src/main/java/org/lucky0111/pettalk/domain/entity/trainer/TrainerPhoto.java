package org.lucky0111.pettalk.domain.entity.trainer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "trainer_photos")
public class TrainerPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @Column(length = 1000)
    private String fileUrl;
    private Integer photoOrder; // 사진 표시 순서 (예: 1, 2)

    public static TrainerPhoto from(String fileUrl, int photoOrder) {
        TrainerPhoto newPhoto = new TrainerPhoto();
        newPhoto.setFileUrl(fileUrl);
        newPhoto.setPhotoOrder(photoOrder);
        return newPhoto;
    }
}
