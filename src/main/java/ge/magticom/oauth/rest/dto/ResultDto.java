package ge.magticom.oauth.rest.dto;


import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResultDto {

    private String message;

    private List<String> items;

    private List<String> attributes;

}
