import React, { Component } from 'react';
import "antd/dist/antd.css";
import {Button, Modal, Progress, InputNumber} from "antd";
import { cartService } from '../CartService';
import {profileService} from "../../Profile/ProfileService";


class CartModal extends Component {

    constructor(props) {
        super(props);

        this.state = {
            visible: false,
            total: 0,
            userData: {},
            percent: 0,
            payed: false,
            products: '',
            discounted: false,
            discount: 0
        };
    }

    handleCancel = e => {
        this.setState({
            visible: false,
        });
    };

    getUserData = () => {
        profileService.getUserData().then( data => {
            let userData = Object.assign({}, data);
            this.setState({userData: userData});
        });
    }

    show = (total, products) => {
        this.getUserData();
        this.setState({visible: true, total: total - this.state.discount, products: products});
    }

    payAll = async () => {
        cartService.createPayment().then(resp => {
            let paymentId = resp.paymentId;
            let user = JSON.parse(localStorage.getItem('user'));
            let userId = user.user_id;
            let products = this.state.products;
            let status = 'pending';

            cartService.createOrder({
                userId: userId,
                paymentId: paymentId,
                products: products,
                status: status
            });
        });

        for (let i = 0; i <= 100; i++) {
            await new Promise(r => setTimeout(r, 50));
            this.setState({percent: i, payed: true});
        }
        this.setState({visible: false, discount: 0});
        this.props.paymentCompleted();
    }

    onCodeInput = () => {
        let discountCode = this.refs.discountCodeInput.input.value;
        cartService.checkDiscountCode(discountCode, this.state.products).then(resp => {
            if (resp) {
                let discount = parseInt((this.state.total * 10) / 100, 10);
                this.setState({total: this.state.total - discount, discounted: true, discount: discount});
            }
            else {
                alert("Provided discount code for ordered products doesn't exists");
            }
        });
    }

    render() {
        return(
            <Modal
                title="Summary"
                visible={this.state.visible}
                footer={false}
                onCancel={this.handleCancel}
                width={'50%'}
                className='cartModal'
            >
                <Button
                    size='middle'
                    disabled={this.state.discounted}
                    onClick={e => this.onCodeInput()}
                    style={{float: 'right', marginLeft: '10px'}}>
                    Add discount code
                </Button>
                <InputNumber
                    ref='discountCodeInput'
                    min={0}
                    disabled={this.state.discounted}
                    style={{float: 'right', width: '140px'}} />

                <strong>Ordering person: </strong>{this.state.userData.firstName} {this.state.userData.secondName}
                <br />
                <strong>Address: </strong>{this.state.userData.address}
                <br/>
                { this.state.percent !== 0 ?
                    <Progress percent={this.state.percent} />: null
                }
                <div style={{textAlign: 'left', marginTop: '10px', fontSize: 'x-large'}}>
                    <strong>Total: </strong>{this.state.total} zł
                    <Button
                        type="primary"
                        size='large'
                        disabled={this.state.payed}
                        onClick={e => this.payAll()}
                        style={{float: 'right'}}>
                        Pay
                    </Button>
                </div>
            </Modal>
        );
    }

}

export default CartModal;