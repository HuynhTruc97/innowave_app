package com.nhom11.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom11.models.Address;
import com.nhom11.innowave.databinding.ItemAddressBinding;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<Address> addressList;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEdit(Address address);
        void onDelete(Address address);
        default void onSelect(Address address) {}
    }

    private boolean enableSelect = false;
    public AddressAdapter(List<Address> addressList, OnAddressActionListener listener) {
        this(addressList, listener, false);
    }
    public AddressAdapter(List<Address> addressList, OnAddressActionListener listener, boolean enableSelect) {
        this.addressList = addressList;
        this.listener = listener;
        this.enableSelect = enableSelect;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressBinding binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.binding.tvName.setText(address.getRecipientName());
        holder.binding.tvPhone.setText(address.getPhoneNumber());
        String fullAddress = address.getStreetAddress() + ", " + address.getWard() + ", " + address.getDistrict() + ", " + address.getProvince();
        holder.binding.tvAddress.setText(fullAddress);
        holder.binding.tvDefault.setVisibility(address.isDefault() == 1 ? View.VISIBLE : View.GONE);
        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(address));
        if (enableSelect) {
            holder.binding.getRoot().setOnClickListener(v -> listener.onSelect(address));
        }
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public Address getAddressAt(int position) {
        return addressList.get(position);
    }

    public void removeAt(int position) {
        addressList.remove(position);
        notifyItemRemoved(position);
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        ItemAddressBinding binding;
        public AddressViewHolder(ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 