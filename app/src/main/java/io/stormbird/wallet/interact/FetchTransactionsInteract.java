package io.stormbird.wallet.interact;

import io.stormbird.wallet.entity.*;
import io.stormbird.wallet.repository.TokenRepositoryType;
import io.stormbird.wallet.repository.TransactionRepositoryType;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.stormbird.wallet.service.TokensService;

public class FetchTransactionsInteract {

    private final TransactionRepositoryType transactionRepository;
    private final TokenRepositoryType tokenRepository;

    public FetchTransactionsInteract(TransactionRepositoryType transactionRepository,
                                     TokenRepositoryType tokenRepositoryType) {
        this.transactionRepository = transactionRepository;
        this.tokenRepository = tokenRepositoryType;
    }

    public Observable<Transaction[]> fetchCached(Wallet wallet) {
        return transactionRepository
                .fetchCachedTransactions(wallet)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Transaction[]> fetchNetworkTransactions(NetworkInfo networkInfo, Wallet wallet, long lastBlock, String userAddress) {
        return transactionRepository
                .fetchNetworkTransaction(networkInfo, wallet, lastBlock, userAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Transaction[]> storeTransactions(Wallet wallet, Transaction[] txList)
    {
        return transactionRepository.storeTransactions(wallet, txList);
    }

    public Single<ContractType> queryInterfaceSpec(TokenInfo tokenInfo)
    {
        return  tokenRepository.resolveProxyAddress(tokenInfo)
                .flatMap(address -> transactionRepository.queryInterfaceSpec(address, tokenInfo));
    }

    public Transaction fetchCached(String walletAddress, String hash)
    {
        return transactionRepository.fetchCachedTransaction(walletAddress, hash);
    }

    public Observable<TokenInfo> queryInterfaceSpecForService(TokenInfo tokenInfo)
    {
        return queryInterfaceSpec(tokenInfo).toObservable()
                .map(spec -> addSpecToService(tokenInfo, spec));
    }

    private TokenInfo addSpecToService(TokenInfo info, ContractType contractType)
    {
        TokensService.setInterfaceSpec(info.address, contractType);
        return info;
    }
}
