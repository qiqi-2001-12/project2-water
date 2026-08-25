package JavaType;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class FragAdapter extends FragmentPagerAdapter
{

    private List<Fragment> list;

    public FragAdapter(FragmentManager fm)
    {
        super(fm);
    }

    public FragAdapter(FragmentManager fm, List<Fragment> list)
    {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int postion)
    {
        return list.get(postion);
    }

    @Override
    public int getCount() {
        return list.size();
    }

}
