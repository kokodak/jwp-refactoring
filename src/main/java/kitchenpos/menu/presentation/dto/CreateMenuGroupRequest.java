package kitchenpos.menu.presentation.dto;

public class CreateMenuGroupRequest {

    private String name;

    private CreateMenuGroupRequest() {
    }

    public CreateMenuGroupRequest(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}