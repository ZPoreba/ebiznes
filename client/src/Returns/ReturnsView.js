import React, { Component } from 'react';
import './style.css';
import "antd/dist/antd.css";
import {Button, List, Spin} from "antd";
import {returnsService} from './ReturnsService';
import {productsService} from "../Products/ProductsService";


class ReturnsView extends Component {

    constructor(props) {
        super(props);

        this.state = {
            products: [],
            loading: true
        };
    }

    componentDidMount() {
        this.loadProducts();
    }

    loadProducts = () => {
        returnsService.getReturnsProducts().then( async ids => {
            let products = [];

            let promises = ids.returns.map( async retProdId => {

                let product = await returnsService.getProductById(retProdId[1]);
                products.push({
                    returnId: retProdId[0],
                    status: retProdId[2],
                    ...product.product
                })

                return product;
            });

            Promise.all(promises).then(() => {
                this.setState({products: products, loading: false});
            });

        });
    }

    cancelReturn = (returnId) => {
        let newProducts = this.state.products;
        newProducts = newProducts.filter( product => {return product.returnId !== returnId});

        returnsService.deleteReturn(returnId).then((resp) => {
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
                                        { item.status === 'pending' ?
                                            <Button
                                                onClick={e => this.cancelReturn(item.returnId)}>
                                                Cancel return
                                            </Button>: null
                                        }
                                        <br />
                                        <br />
                                        Status: {item.status}
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

export default ReturnsView;