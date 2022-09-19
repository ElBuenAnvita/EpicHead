package es.raxthelag.epichead.controllers;

import es.raxthelag.epichead.Main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;

public class EconomyHolder implements Economy {
    public String CURRENCY_SYMBOL = Main.getInstance().getConfig().getString("economy.currency-symbol", "$");
    public char GROUPING_SEPARATOR = Main.getInstance().getConfig().getString("economy.grouping-separator", ".").charAt(0);
    public char DECIMAL_SEPARATOR = Main.getInstance().getConfig().getString("economy.decimal-separator", ",").charAt(0);
    public int DECIMAL_PLACES = Main.getInstance().getConfig().getInt("economy.fraction-digits", 4);
    public int DECIMAL_PLACES_SHOWN = Main.getInstance().getConfig().getInt("economy.fraction-digits-shown", 2);

    /**
     * Checks if economy method is enabled.
     *
     * @return Success or Failure
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Gets name of economy method
     *
     * @return Name of Economy Method
     */
    @Override
    public String getName() {
        return "EpicEconomy";
    }

    /**
     * Returns true if the given implementation supports banks.
     *
     * @return true if the implementation supports banks
     */
    @Override
    public boolean hasBankSupport() {
        return false;
    }

    /**
     * Some economy plugins round off after a certain number of digits.
     * This function returns the number of digits the plugin keeps
     * or -1 if no rounding occurs.
     *
     * @return number of digits after the decimal point kept
     */
    @Override
    public int fractionalDigits() {
        // return 2;
        // return Main.getInstance().getConfig().getInt("economy.fraction-digits", 2);
        return DECIMAL_PLACES;
    }

    /**
     * Format amount into a human-readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.
     *
     * @param amount to format
     * @return Human-readable string describing amount
     */
    @Override
    public String format(double amount) {
        /* NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol(CURRENCY_SYMBOL);
        dfs.setGroupingSeparator(GROUPING_SEPARATOR);
        dfs.setMonetaryDecimalSeparator(DECIMAL_SEPARATOR);
        df.setMaximumFractionDigits(fractionalDigits());

        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);
        return df.format(amount); */
        return format(BigDecimal.valueOf(amount));
    }

    /**
     * Format amount into a human-readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.
     *
     * @param amount to format
     * @return Human-readable string describing amount
     */
    public String format(BigDecimal amount) {
        NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol(CURRENCY_SYMBOL);
        dfs.setGroupingSeparator(GROUPING_SEPARATOR);
        dfs.setMonetaryDecimalSeparator(DECIMAL_SEPARATOR);
        df.setMaximumFractionDigits(DECIMAL_PLACES_SHOWN);

        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);
        return df.format(amount);
    }

    /**
     * Returns the name of the currency in plural form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (plural)
     */
    @Override
    public String currencyNamePlural() {
        return Main.getInstance().getConfig().getString("economy.currency-name-plural", "$");
    }

    /**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (singular)
     */
    @Override
    public String currencyNameSingular() {
        return Main.getInstance().getConfig().getString("economy.currency-name", "$");
    }

    /**
     * @param playerName Name of the player
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player to check
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    /**
     * @param playerName player to check
     * @param worldName name of the world
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return true;
    }

    /**
     * Checks if this player has an account on the server yet on the given world
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player    to check in the world
     * @param worldName world-specific account
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)} instead.
     */
    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    /**
     * Gets balance of a player
     *
     * @param player of the player
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(OfflinePlayer player) {
        if (player.getName() == null) return 0;

        EpicPlayer epicPlayer = EpicPlayer.get(player.getName());
        if (epicPlayer.isLoaded() && epicPlayer.isOnline()) {
            return epicPlayer.getBalance().doubleValue();
        } else if (!epicPlayer.isLoaded() && !epicPlayer.isOnline()) {
            try {
                Main.getInstance().getDataConnection().loadPlayer(epicPlayer);
                return epicPlayer.getBalance().doubleValue();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    /**
     * @param playerName
     * @param world
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer, String)} instead.
     */
    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(Bukkit.getOfflinePlayer(playerName), world);
    }

    /**
     * Gets balance of a player on the specified world.
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player to check
     * @param world  name of the world
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    /**
     * @param playerName name of the player
     * @param amount money to check
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)} instead.
     */
    @Override
    public boolean has(String playerName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    /**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to check
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        if (player.getName() == null) return false;
        // TODO UUID CHECK
        BigDecimal bigDecimalAmount = BigDecimal.valueOf(amount);
        BigDecimal playerBalance;

        EpicPlayer epicPlayer = EpicPlayer.get(player.getName());
        try {
            if (epicPlayer.isLoaded()) {
                playerBalance = epicPlayer.getBalance();
            } else {
                Main.getInstance().getDataConnection().loadPlayer(epicPlayer);
                playerBalance = epicPlayer.getBalance();

                if (!epicPlayer.isOnline()) EpicPlayer.remove(epicPlayer.getName());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return (playerBalance.compareTo(bigDecimalAmount) >= 0);
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use @{link {@link #has(OfflinePlayer, String, double)} instead.
     */
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), worldName, amount);
    }

    /**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to check
     * @param worldName to check with
     * @param amount    to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to withdraw from
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player.getName() == null) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Unknown player name");
        // TODO UUID CHECK

        EpicPlayer epicPlayer = EpicPlayer.get(player.getName());
        try {
            if (epicPlayer.isLoaded()) {
                epicPlayer.withdraw(amount);
            } else {
                Main.getInstance().getDataConnection().loadPlayer(epicPlayer);
                epicPlayer.withdraw(amount, true);

                if (!epicPlayer.isOnline()) EpicPlayer.remove(epicPlayer.getName());
            }
            return new EconomyResponse(amount, epicPlayer.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Error while saving");
        }
        // return null;
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), worldName, amount);
    }

    /**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to withdraw from
     * @param worldName - name of the world
     * @param amount    Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to deposit to
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player.getName() == null) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Unknown player name");
        // TODO UUID CHECK

        EpicPlayer epicPlayer = EpicPlayer.get(player.getName());
        try {
            if (epicPlayer.isLoaded()) {
                epicPlayer.deposit(amount);
            } else {
                Main.getInstance().getDataConnection().loadPlayer(epicPlayer);
                epicPlayer.deposit(amount, true);

                if (!epicPlayer.isOnline()) EpicPlayer.remove(epicPlayer.getName());
            }
            return new EconomyResponse(amount, epicPlayer.getBalance().doubleValue(), EconomyResponse.ResponseType.SUCCESS, "");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Error while saving");
        }
        // return null;
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), worldName, amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to deposit to
     * @param worldName name of the world
     * @param amount    Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    /**
     * @param name
     * @param player
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse createBank(String name, String player) {
        return null;
    }

    /**
     * Creates a bank account with the specified name and the player as the owner
     *
     * @param name   of account
     * @param player the account should be linked to
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return null;
    }

    /**
     * Deletes a bank account with the specified name.
     *
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    @Override
    public EconomyResponse deleteBank(String name) {
        return null;
    }

    /**
     * Returns the amount the bank has
     *
     * @param name of the account
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankBalance(String name) {
        return null;
    }

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to check for
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to withdraw
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }

    /**
     * @param name
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    /**
     * Check if a player is the owner of a bank account
     *
     * @param name   of the account
     * @param player to check for ownership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return null;
    }

    /**
     * @param name
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankMember(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    /**
     * Check if the player is a member of the bank account
     *
     * @param name   of the account
     * @param player to check membership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return null;
    }

    /**
     * Gets the list of banks
     *
     * @return the List of Banks
     */
    @Override
    public List<String> getBanks() {
        return null;
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }

    /**
     * Attempts to create a player account for the given player
     *
     * @param player OfflinePlayer
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return false;
    }

    /**
     * @param playerName
     * @param worldName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return false;
    }

    /**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this then false will always be returned.
     *
     * @param player    OfflinePlayer
     * @param worldName String name of the world
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return false;
    }
}
