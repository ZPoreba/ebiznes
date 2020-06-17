import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import {Button, List, Spin} from "antd";
import { wishlistService } from './WishListService';
import {productsService} from "../Products/ProductsService";
import {withRouter} from "react-router-dom";
import {commonService} from "../Common/CommonService";


class WishListView extends Component {

    constructor(props) {
        super(props);

        this.state = {
            products: [],
            loading: true
        };
    }

    componentDidMount() {
        commonService.checkIfAuthenticated(this.props).then(result => {
            if(result) this.loadProducts();
        });
    }

    loadProducts = () => {
        wishlistService.getWishListProducts().then( async ids => {
            let products = [];

            let promises = ids.products.map( async productId => {
                let product = await wishlistService.getProductById(productId);

                if(products.some(p => p.id === product.product.id)) {
                    let filteredId= products.findIndex(p => p.id === product.product.id);
                    products[filteredId].counter = products[filteredId].counter + 1;
                }
                else {
                    product.product.counter = 1;
                    products.push(product.product);
                }

                return product;
            });

            Promise.all(promises).then(() => {
                this.setState({products: products, loading: false});
            });

        });
    }

    deleteFromBucket = (productId) => {
        let newProducts = this.state.products;
        newProducts = newProducts.filter( product => {return product.id !== productId});

        wishlistService.deleteProductForUser(productId).then((resp) => {
            alert(resp);
            this.setState({products: newProducts});
        });
    }

    addToBucket = (productId) => {
        productsService.addToBucket(productId).then((resp) => {
            alert(resp);
        });
    }

    render() {
        return(
            <div className="bucketView">
                <Spin spinning={this.state.loading} className='spin' size="large">
                    <List
                        itemLayout="vertical"
                        size="large"
                        locale={{emptyText: 'No products in bucket'}}
                        style={{marginLeft: '25%', marginRight: '25%'}}
                        pagination={{
                            size: "small",
                            pageSize: 6,
                        }}
                        dataSource={this.state.products}
                        renderItem={item => (
                            <List.Item
                                key={item.id}
                                style={{textAlign: "left"}}
                                extra={
                                    <div>
                                        <Button onClick={ e => this.addToBucket(item.id) }>Add to cart</Button>
                                        <br/>
                                        <br/>
                                        <Button onClick={ e => this.deleteFromBucket(item.id) }>Delete from wish list</Button>
                                    </div>
                                }
                            >
                                <List.Item.Meta
                                    style={{textAlign: "left"}}
                                    title={<strong>{item.name}</strong>}
                                    description={<strong>Price: {item.price} zł, Amount: {item.counter}</strong>}
                                />
                                {item.description}
                            </List.Item>
                        )}
                    />
                </Spin>
            </div>
        )
    }
}

export default withRouter(WishListView);