package cz.cesnet.shongo.controller.rest.models.resource;

import cz.cesnet.shongo.controller.api.ResourceSummary;
import cz.cesnet.shongo.controller.api.Tag;
import cz.cesnet.shongo.controller.rest.models.TechnologyModel;
import lombok.Data;

import java.util.Set;

/**
 * Represents {@link ResourceSummary}.
 *
 * @author Filip Karnis
 */
@Data
public class ResourceModel
{

    private String id;
    private ResourceSummary.Type type;
    private String name;
    private String description;
    private TechnologyModel technology;
    private Set<Tag> tags;
    private boolean hasCapacity;

    public ResourceModel(ResourceSummary summary)
    {
        this.id = summary.getId();
        this.type = summary.getType();
        this.name = summary.getName();
        this.description = summary.getDescription();
        this.technology = TechnologyModel.find(summary.getTechnologies());
        this.tags = summary.getTags();
        this.hasCapacity = summary.hasCapacity();
    }
}
