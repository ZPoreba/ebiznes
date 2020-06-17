import { compose, createStore } from 'redux';
import { makeRootReducer } from './reducers';


export default (initialState = {}) => {
    const enhancers = [];
    let composeEnhancers = compose;

    const store = createStore(
        makeRootReducer(),
        initialState,
        composeEnhancers(
            ...enhancers,
        ),
    );
    store.asyncReducers = {};
    store.asyncSagas = {};

    if (module.hot) {
        module.hot.accept('./reducers', () => {
            const reducers = require('./reducers').default;
            store.replaceReducer(reducers(store.asyncReducers));
        });
    }

    return store;
};
