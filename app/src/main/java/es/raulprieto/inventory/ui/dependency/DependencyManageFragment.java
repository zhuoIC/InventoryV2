package es.raulprieto.inventory.ui.dependency;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import es.raulprieto.inventory.R;
import es.raulprieto.inventory.data.db.model.Dependency;
import es.raulprieto.inventory.databinding.FragmentDependencyManageBinding;

/**
 * Style:
 * https://material.io/develop/android/components/text-input-layout/
 */
public class DependencyManageFragment extends Fragment implements DependencyManageContract.View {
    static final String TAG = "dependencyaddfragment";

    private DependencyManageContract.Presenter dependencyManagePresenter;

    private FragmentDependencyManageBinding binding;
    private FloatingActionButton fab;
    //    ConstraintSet constraintSet;
    private Dependency dependency;


    public DependencyManageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bundle Arguments if they exists
     * @return A new instance of fragment DependencyManageFragment.
     */
    static Fragment newInstance(Bundle bundle) {
        DependencyManageFragment fragment = new DependencyManageFragment();
        if (bundle != null)
            fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dependency_manage, container, false);

        View view = binding.getRoot();

        String title = "Add Dependency";
        Bundle bundle = getArguments();
        if (bundle != null) {
            dependency = (Dependency) bundle.getSerializable("dependency");
            binding.setDependency(dependency);
            binding.tedDependencyShortName.setEnabled(false); // RN-D5: ShortName can't be updated
            setSpinnerSelection();
            title = "Edit Dependency";
        }

        Toolbar toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setTitle(title);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back, null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
            }
        });

        fab = getActivity().findViewById(R.id.fab);

//        changeDescriptionTilConstraint();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFab();
    }

    /* TO CHECK, the constraint set works, but It won't find the fab as it has already got another parent

    private void changeDescriptionTilConstraint() {
        // Boton no está en el constraint
        constraintSet = new ConstraintSet();
        constraintSet.clone(binding.constraintLayout);

        constraintSet.connect(binding.tilDependencyDescription.getId(), ConstraintSet.BOTTOM, fab.getId(), ConstraintSet.TOP, 0);
//        constraintSet.constrainDefaultHeight(fab.getId(), 200);
        constraintSet.applyTo(binding.constraintLayout);
    }*/

    /**
     * Collects the data and creates a dependency object, if needed.
     *
     * @return dependency object
     */
    private Dependency getDependency() {
        if (dependency == null)
            dependency = new Dependency();

        dependency.setName(Objects.requireNonNull(binding.tedDependencyName.getText()).toString());
        dependency.setShortName(Objects.requireNonNull(binding.tedDependencyShortName.getText()).toString());
        dependency.setInventory(binding.spInventory.getSelectedItem().toString());
        dependency.setDescription(Objects.requireNonNull(binding.tedDependencyDescription.getText()).toString());

        return dependency;
    }

    /**
     * Method where the fab's onClick is initialized.
     * <p>
     * The method dependencyManagePresenter.validateDependency will only return false if
     * there were any errors with the ShortName so, in order to let the User know,
     * the error will be set at the TextInputLayout
     */
    private void initializeFab() {
        fab.show();
        fab.setImageResource(R.drawable.ic_action_save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDependencyValid())
                    if (!dependencyManagePresenter.validateDependency(getDependency()))
                        binding.tilDependencyShortName.setError(getString(R.string.errInvalidShortName));
            }
        });
    }

    /**
     * Locates the position of the Dependency's Inventory at the string-array from resources
     * so the spinner selection is set to the correct position
     */
    private void setSpinnerSelection() {
        List<String> inventoryArray = Arrays.asList(getResources().getStringArray(R.array.inventoryArray));
        int position = inventoryArray.indexOf(dependency.getInventory());

        if (position != -1) // Check if dependency hasn't got an inventory defined
            binding.spInventory.setSelection(position);
    }

    /**
     * Checks the Dependency Model business rules
     * RN1: no empty fields
     *
     * @return boolean
     */
    private boolean isDependencyValid() {
        boolean isValid = true;

        if (TextUtils.isEmpty(Objects.requireNonNull(binding.tedDependencyDescription.getText()).toString())) {
            binding.tilDependencyDescription.setError(getString(R.string.errDescriptionEmpty));
            isValid = false;
            // TODO getfocus on error fields
        } else
            binding.tilDependencyDescription.setError(null);

        if (TextUtils.isEmpty(Objects.requireNonNull(binding.tedDependencyShortName.getText()).toString())) {
            binding.tilDependencyShortName.setError(getString(R.string.errShortNameEmpty));
            isValid = false;
        } else
            binding.tilDependencyShortName.setError(null);

        if (TextUtils.isEmpty(Objects.requireNonNull(binding.tedDependencyName.getText()).toString())) {
            binding.tilDependencyName.setError(getString(R.string.errNameEmpty));
            isValid = false;
        } else
            binding.tilDependencyName.setError(null);

        return isValid;
    }


    //region DependencyManageContract

    /**
     * It's called from the Presenter after checking if the Dependency is correct
     */
    @Override
    public void onSuccessValidate() {
        Dependency dependency = getDependency();
        if (getArguments() != null)
            dependencyManagePresenter.edit(dependency);
        else
            dependencyManagePresenter.add(dependency);
    }

    @Override
    public void setManagePresenter(DependencyManageContract.Presenter dependencyManagePresenter) {
        this.dependencyManagePresenter = dependencyManagePresenter;
    }

    @Override
    public void showError(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
    }

    /**
     * It is called from the Presenter after finishing an add/edit action and shows the list.
     */
    @Override
    public void onSuccess() {
        Objects.requireNonNull(getActivity()).onBackPressed();
    }
    //endregion

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /*try {
            onDependencySaveListener = (OnDependencySaveListener) context;
        } catch (Exception e) {
            throw new RuntimeException(context.toString()
                    + " must implement OnDependencySaveListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        onDependencySaveListener = null;
    }
}