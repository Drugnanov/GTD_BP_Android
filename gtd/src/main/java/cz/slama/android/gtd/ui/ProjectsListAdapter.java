package cz.slama.android.gtd.ui;

import android.view.LayoutInflater;

import cz.slama.android.gtd.R;
import cz.slama.android.gtd.core.News;
import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.util.StateUtils;

import java.util.List;

public class ProjectsListAdapter extends AlternatingColorListAdapter<Project> {


    /**
     * @param inflater
     * @param items
     * @param selectable
     */

    public ProjectsListAdapter(final LayoutInflater inflater, final List<Project> items,
                               final boolean selectable) {
        super(R.layout.project_list_item, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public ProjectsListAdapter(final LayoutInflater inflater, final List<Project> items) {
        super(R.layout.project_list_item, inflater, items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.tv_title, R.id.tv_summary, R.id.tv_overview_project_state};
        //                ,R.id.tv_date
    }

    @Override
    protected void update(final int position, final Project item) {
        super.update(position, item);

        setText(0, item.getTitle());
        setText(1, item.getDescription());
        setText(2, StateUtils.getStateName(StateUtils.EStateTypes.PROJECT, item.getState()));
        //setNumber(R.id.tv_date, item.getCreatedAt());
    }
}
